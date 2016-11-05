/*global define */

'use strict';

define(function() {

/* Controllers */

var controllers = {};

controllers.MyCtrl1 = function($scope) 
{

    $scope.login = function() {
        alert('this is scope speaking');
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

controllers.ItemsCtrl = function($scope, $mdDialog, $mdToast, Item, Message, User) 
{
    $scope.items = {};
    //this is what is loaded when the Items page is ready
    //we get all the items
    angular.element(document).ready(function () {
        Item.getAllItems(function(response){
            $scope.items = response;
        });
    });

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

        if ($rootScope.isLoggedIn) {
            $scope.canBuy = true; //we have a logged user
        }

        Item.getItemWithID(selectedItemID, function(item) {
            $scope.item = item;
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
controllers.ItemsCtrl.$inject = ['$scope', '$mdDialog', '$mdToast', 'Item', 'Message', 'User'];

controllers.AccountCtrl = function($scope, $rootScope, User, Item, Message, $mdToast, $mdDialog) 
{
    $scope.isLoggedIn = false;

    $scope.myItems = [];
    $scope.showAccountPart = 'profile';

    $scope.username = User.getLoggedUserName();
    $scope.password = User.getPassword();

    $scope.itemName = "";
    $scope.itemDescription = "";
    $scope.itemPrice = "";

    $scope.changeAccountView = function(view) {
        $scope.showAccountPart = view;
    };

    //triggered on page load
    angular.element(document).ready(function () {
        if ($rootScope.isLoggedIn) {
            Item.getMyItems($scope.username, function(response) {
                $scope.myItems = response;
            });
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
        Item.uploadItem($scope.username, $scope.itemName, $scope.itemDescription, 
            $scope.itemPrice, 'photo', function(newItem) {
            if (newItem !== undefined) {
                //it was successful we can show a message that it was successful
                //empty out the inputs
                $scope.itemName = "";
                $scope.itemDescription = "";
                $scope.itemPrice = "";
                $mdToast.show($mdToast.simple().content("Item was uploaded successfuly!"));
                $scope.myItems.push(newItem)
            }
        });
    };

    //when view details is clicked
    $scope.seeItemDetails = function(itemID) {
        $mdDialog.show({
           locals : {selectedItemID: itemID},
           controller : itemSelectedCtrl,
           templateUrl : 'views/account-item-details.html',
        });
    };

    //the controller that controlls the popup
    var itemSelectedCtrl = function($scope, selectedItemID) {
        $scope.showNoMessagesForItem = false;
        $scope.messages = [];
        //on pop up start
        angular.element(document).ready(function () {
            //get the selected item
            Item.getItemWithID(selectedItemID, function(item) {
                $scope.item = item;
            });
            
            //we should get the messages for this item
            Message.getMessagesForItemID(selectedItemID, function(messages) {
                console.log("THIS IS THE MESSAGE >>>>>>>>>>>>>>");
                console.log(messages);
                console.log("THIS IS THE MESSAGE >>>>>>>>>>>>>>");
                if (messages.length == 0) {
                    $scope.showNoMessagesForItem = true;
                }
                else {
                    $scope.messages = messages;
                }
            });
        });
        
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