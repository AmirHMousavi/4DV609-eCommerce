/*global define */

'use strict';

define(function() {

/* Controllers */

var controllers = {};

controllers.MyCtrl1 = function($scope) 
{

    $scope.login = function() {
    }
}

controllers.MyCtrl1.$inject = ['$scope'];

controllers.MyCtrl2 = function() 
{

}
controllers.MyCtrl2.$inject = [];

controllers.LoginCtrl = function($scope, $rootScope, $mdToast, Config, User) 
{
    $scope.showLoginForm = true;

    /**
     * toggleForm - it switches between login and register form
     */
    $scope.toggleForm = function() {
        $scope.showLoginForm = ! $scope.showLoginForm;
    }

    /**
     * login - it is triggered when user clicks on login button
     */
    $scope.login = function() {
        //alert('we are trying to run the damn login function :: ' + Config.url);
        User.logIn($scope.username, $scope.password, function(status) {
            if (status == 200) {
                //the login was successful we can store the data in local cookie
                //then we should redirect to the other view where we show the user account
               $rootScope.currentUser = User.getUserName();
               $rootScope.isLoggedIn = true;
               window.location = "#/item";
            }
            else {
                //something went wrong we should notify the user
                //username or password is wrong
                alert(status);
            }
        });
    }

    /**
    * register - it is triggered when user clicks on register button
    */
    $scope.register = function() {
        if ($scope.username != undefined && $scope.password != undefined && $scope.username.length > 0 && $scope.password.length > 0) {
            User.register($scope.username, $scope.password, function(response) {
                if (response.userId.length > 0) {
                    //user was registered successfully
                    $mdToast.show($mdToast.simple().content("You were registered successfully!"));
                    User.logIn($scope.username, $scope.password, function(status) {
                        if (status == 200) {
                            //the login was successful we can store the data in local cookie
                            //then we should redirect to the other view where we show the user account
                            $rootScope.currentUser = User.getUserName();
                            $rootScope.isLoggedIn = true;
                            window.location = "#/item";
                        }
                        else {
                            //something went wrong we should notify the user
                            //username or password is wrong
                            alert(status);
                        }
                    });
                }
            });
        }
        else {
            alert('Something went wrong...');
            return;
        }
    }

}

controllers.LoginCtrl.$inject = ['$scope', '$rootScope', '$mdToast', 'Config','User'];

controllers.ItemsCtrl = function($scope, $rootScope, $q, $mdDialog, $mdToast, Item, Message, User) 
{
    $scope.showItems = false;
    $scope.items = [];
    //this is what is loaded when the Items page is ready
    //we get all the items
    angular.element(document).ready(function () {
        //get all the items
        Item.getAllItems(function(response){
            $scope.items = response;
            var itemImagesFetched = 0;
            //go through all the items and get their image
            //IT WOULD BE BETTER IF WE DO THIS IN Items class
            angular.forEach($scope.items, function(item) {
                itemImagesFetched ++;
                Item.downloadImageForItem(item.id, function(imageData) {
                    //get the item img element and update the src to show the picture
                    //document.getElementById(item.id).src = imageData;
                    item['dataURL'] = imageData;
                    //check if all item images have been fetched
                    if (itemImagesFetched == $scope.items.length) {
                        //all item images are shown now we can 
                        //display the items
                        $scope.showItems = true;
                        //console.log($scope.items);
                        //since we retrieve the images using web sockets
                        //the angular digest system isn't aware of the changes
                        //and it wont update the HTML if we don't call $scope.apply()
                        $scope.$apply();
                    }
                });

                User.getUserRating(item.userId, function(rating) {
                    item['userRating'] = rating;
                    //document.getElementById('rating_' + item.id).innerHTML = "Rating : " +rating;
                });
            });
        });

        //after we get the items we check if we have something to rate
        Message.getUserMessages($rootScope.currentUser, function(messages) {
            Message.getMessagesOfItemsSold(messages, function(msgs) {
                var itemsAndRatings = [];
                if (msgs.length > 0) {
                    var defer = $q.defer();
                    angular.forEach(msgs, function(msg) {
                        Item.getItemWithRatings(msg.isSold, function(item) {
                            //console.log(response);
                            item['msg'] = msg;
                            defer.resolve(itemsAndRatings.push(item));
                        });
                    });

                    defer.promise.then(function(result) {
                        //if we are still logged in 
                        if ($rootScope.isLoggedIn == true) {
                            $mdDialog.show({
                                locals : {itemsToRate: itemsAndRatings},
                                controller : itemsToRateCtrl,
                                templateUrl : 'views/items-rate.html',
                            });
                        }
                    });
                }
            });
     });
    });
    //this is also defined in the MAIN ... its not the best practice
    function itemsToRateCtrl($scope, $rootScope, $mdToast, itemsToRate) {
        //console.log(itemsToRate);
        $scope.rateItems = itemsToRate;
        //console.log($scope.rateItems);
        $rootScope.itemRated = [];
        $scope.closePopUp = function() {
            $mdDialog.cancel();
        }

        $scope.rateThisItem = function(rate, rateID, messageID, itemID) {
            $rootScope.itemRated.push(rateID);
            Item.rateThisItem(rate, rateID, messageID, itemID, User.getLoggedUserName(), function(response) {
                //console.log('the rating is done t his is the response');
                console.log(response);
                if (response.done == true) {
                    $mdToast.show($mdToast.simple().content("ITEM WAS RATED SUCCESSFULLY"));
                }
            });
        }

        $scope.onHover = function(rateIndex, row) {
            for (var i = 1; i <= 5; i++) {
                var starName = i.toString() + '_' + row;
                if ($rootScope.itemRated.indexOf(row) == -1) {
                    if (i <= parseInt(rateIndex)) {
                        document.getElementById(starName).style.fill = "yellow";
                    }
                    else {
                        document.getElementById(starName).style.fill = "gray";
                    }
                }
            }
            //clear the other rows
            angular.forEach($scope.rateItems, function(item) {
                if ($rootScope.itemRated.indexOf(item.ratingID) == -1) {
                    if (item.ratingID != row) {
                        for (var j = 1; j <= 5; j++) {
                            var starName = j.toString() + '_' + item.ratingID;
                            document.getElementById(starName).style.fill = "gray";
                        }
                    }
                }
            });
        }
    }

    /**
     * itemDetails - shows the popup with the information of the item
     * triggered when the button view details is clicked
     */
    $scope.itemDetails = function(itemID) {
        $mdDialog.show({
           locals : {selectedItemID: itemID},
           controller : itemSelectedCtrl,
           templateUrl : 'views/item-details.html',
        });
    };

    //the controller that controlls the popup
    var itemSelectedCtrl = function($scope, $rootScope, selectedItemID) {
        $scope.canBuy = false;
        $scope.displaySendMessage = false;
        $scope.message = "";
        //if the user is seeing the item he uploaded himself
        $scope.isMyItem = false;

        if ($rootScope.isLoggedIn) {
            $scope.canBuy = true; //we have a logged user
        }

        Item.getItemWithID(selectedItemID, function(item) {
            $scope.item = item;
            $scope.showItem = false;

            //get the item image
            Item.downloadImageForItem(item.id, function(imageData) {
                //get the item img element and update the src to show the picture
                document.getElementById("single_item_" + item.id).src = imageData;
                $scope.showItem = true;
                $scope.$apply();
            });

            User.getUserRating(item.userId, function(rating) {
                document.getElementById('single_item_rating_' + item.id).innerHTML = "RATING : " +rating;
            });

            if (item.userId == User.getLoggedUserName()) {
                $scope.isMyItem = true;
            }
        });

        //close the mdDialog popup
        $scope.closePopUp = function() {
            $mdDialog.cancel();
        };

        //when Buy button is clicked
        $scope.iWantToBuy = function() {
            $scope.displaySendMessage = true;
        };

        //when send message is clicked
        $scope.sendMessage = function() {
            var userID = User.getLoggedUserName();
            //send the message
            Message.sendMessageForItemID(userID, selectedItemID, $scope.message, function(message) {
                 $mdToast.show($mdToast.simple().content("Message sent successfully!"));
                 $scope.displaySendMessage = false;
            });
        }
    }
    
}
controllers.ItemsCtrl.$inject = ['$scope', '$rootScope', '$q', '$mdDialog', '$mdToast', 'Item', 'Message', 'User'];

controllers.AccountCtrl = function($scope, $rootScope, User, Item, Message, $mdToast, $mdDialog) 
{
    $scope.isLoggedIn = false;

    $scope.myItems = [];
    $scope.itemsIamInterestedOn = [];
    $scope.showAccountPart = 'profile';

    $scope.username = User.getLoggedUserName();
    $scope.password = User.getPassword();
    $scope.rating = "..";

    $scope.itemName = "";
    $scope.itemDescription = "";
    $scope.itemPrice = "";

    $scope.changeAccountView = function(view) {
        $scope.showAccountPart = view;
    };

    function itemsIAmInterestedOn(callback) {
        Message.getAllMessagesForUser($scope.username, function(messages) {
            Item.getAllItems(function(items) {
                //after we have all the items and all the user messages
                //we loop through messages and get the items with the id of item in message
                angular.forEach(messages, function(message) {
                    angular.forEach(items, function(item) {
                        if (message.itemId == item.id && $scope.username == message.userId) {
                            //check if the item exists in the array
                            itemExists(item.id, $scope.itemsIamInterestedOn, function(response) {
                                if (response.exists == false) {
                                    //i am interested in this 
                                    $scope.itemsIamInterestedOn.push(item);
                                    //get the images for the items
                                    Item.downloadImageForItem(item.id, function(imgData) {
                                        document.getElementById("item_interested_"+item.id).src = imgData;
                                    });
                                }
                            });
                        }
                    });
                });
            });
        });
    }

    function itemExists(itemID, items, callback) {
        var counter = items.length;
        if (counter > 0 ) {
            angular.forEach(items, function(item) {
                if (item.id == itemID) {
                    return callback({exists : true});
                }

                if (counter == 1) {
                    return callback({exists : false});
                }
            });
        }
        else {
            return callback({exists : false});
        }
    }

    //triggered on page load
    angular.element(document).ready(function () {
        if ($rootScope.isLoggedIn) {
            //get the user rating
            User.getUserRating($scope.username, function(rating) {
                $scope.rating = rating;
            });

            Item.getMyItems($scope.username, function(response) {
                $scope.myItems = response;
                var itemImagesFetched = 0;                
                //IT WOULD BE BETTER IF WE DO THIS IN Items class
                angular.forEach($scope.myItems, function(item) {
                    itemImagesFetched ++;
                    Item.downloadImageForItem(item.id, function(imageData) {
                        //get the item img element and update the src to show the picture
                        document.getElementById(item.id).src = imageData;
                        //check if all item images have been fetched
                        if ($scope.items != undefined) {
                            if (itemImagesFetched == $scope.items.length) {
                                //all item images are shown now we can 
                                //display the items
                                $scope.showItems = true;
                            }
                        }
                        //since we retrieve the images using web sockets
                        //the angular digest system isn't aware of the changes
                        //and it wont update the HTML if we don't call $scope.apply()
                        $scope.$apply();
                    });
                });
            });

            //now get all the items
            itemsIAmInterestedOn();

            $scope.isLoggedIn = true;
        }
        else {
            //they are not supposed to see this page
            //document.location = "#/";
            $scope.isLoggedIn = false;
        } 
    });

    //triggered when we want to upload a new item
    $scope.uploadItem = function() {
        $scope.showUploadingItemIndicator = false;
        //we get the file/image we want to upload
        var file    = document.querySelector('input[type=file]').files[0];
        var reader  = new FileReader();

        if (file) {
            //reads the data as a URL
           reader.readAsDataURL(file);
        }

        //when the image is ready to be uploaded
        reader.onloadend = function () {
            $scope.showUploadingItemIndicator = true;
            Item.uploadItem($scope.username, $scope.itemName, $scope.itemDescription, 
                $scope.itemPrice, file.name, function(newItem) {
                if (newItem !== undefined) {
                    //it was successful we can show a message that it was successful
                    //empty out the inputs
                    //then we have to upload the image for the item
                    Item.uploadImageForItem(newItem.itemID, $scope.username, reader.result, function(response) {
                        console.log('image has been uploaded');
                        console.log(response);
                        $scope.showUploadingItemIndicator = false;
                        $scope.itemName = "";
                        $scope.itemDescription = "";
                        $scope.itemPrice = "";
                        $mdToast.show($mdToast.simple().content("ITEM WAS UPLOADED SUCCESSUFLLY!"));
                        $scope.myItems.push(newItem);
                        $scope.$apply();
                    });   
                }
            });
       }

    };

    //when view details is clicked
    $scope.seeItemDetails = function(itemID, viewOption) {
        $mdDialog.show({
           locals : {selectedItemID: itemID, option : viewOption},
           controller : itemSelectedCtrl,
           templateUrl : 'views/account-item-details.html',
        });
    };

    //the controller that controlls the popup
    var itemSelectedCtrl = function($scope, selectedItemID, option) {
        $scope.showNoMessagesForItem = false;
        $scope.showItem = false;
        $scope.messages = [];
        $scope.itemIsSold = false;
        $scope.canSell = false;
        $scope.selectedItemID = selectedItemID;
        //on pop up start
        angular.element(document).ready(function () {
            //get the selected item
            Item.getItemWithID(selectedItemID, function(item) {
                $scope.item = item;
                $scope.itemIsSold = Item.getItemStatus(item);
                console.log('this is the status of the item ::' + $scope.itemIsSold);
                //get the item image
                Item.downloadImageForItem(item.id, function(imageData) {
                    $scope.showItem = true;
                    //get the item img element and update the src to show the picture
                    document.getElementById("single_item_" + item.id).src = imageData;
                    $scope.$apply();
                });
            });

            if (option == 'all') {
                $scope.canSell = true;
                //we should get the messages for this item
                Message.getMessagesForItemID(selectedItemID, function(messages) {
                    if (messages.length == 0) {
                        $scope.showNoMessagesForItem = true;
                    }
                    else {
                        $scope.messages = messages;              
                    }
                });
            }
            else if (option == 'mine') {
                $scope.canSell = false;
                //we should get only users messages for this item 
                //this is called in the tab where we show the items 
                //the user has been interested on
                Message.getAllMessagesForUser( User.getLoggedUserName(), function(messages) {
                    if (messages.length == 0) {
                        $scope.showNoMessagesForItem = true;
                    }
                    else {
                        $scope.messages = messages;              
                    }
                });
            }
            
        });
        
        //the seller clicks one of the messages to sell to the person
        $scope.sellItem = function(itemID, messageID) {
            Item.sellItemWithID(itemID, messageID, User.getUserName(), function(response) {
                //set item to sold in the front end too 
                Message.setMessageIsSold(messageID, function(response){
                    console.log('this is sold....... message...');
                    console.log(response);
                    $scope.itemIsSold = true;
                    $mdDialog.cancel();
                    $mdToast.show($mdToast.simple().content("ITEM WAS SOLD SUCCESSUFLLY! THE BUYER WILL BE NOTIFIED"));
                });
                
            });
        };

        //close the mdDialog popup
        $scope.closePopUp = function() {
            $mdDialog.cancel();
        };
    };

}

controllers.AccountCtrl.$inject = ['$scope', '$rootScope', 'User', 'Item', 'Message', '$mdToast', '$mdDialog'];

controllers.DocumentationCtrl = function($scope, $rootScope, User, Item, $mdToast) {
    //
}

controllers.DocumentationCtrl.$inject = ['$scope', '$rootScope', 'User', 'Item', '$mdToast'];

controllers.UsCtrl = function($scope, $rootScope, User, Item, $mdToast) {
    //
}

controllers.UsCtrl.$inject = ['$scope', '$rootScope', 'User', 'Item', '$mdToast'];

return controllers;

});