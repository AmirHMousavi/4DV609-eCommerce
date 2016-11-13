/*global define */

'use strict';

define(['angular'], function(angular) {

/* Services */

// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('myApp.services', [])
  //value('version', '0.1')
  .factory('Config', [function() {

    var Config = {
        //this should be the hosting URL for NOW is localhost
        url : "http://localhost:9000/api/"
    };

    return Config;
}])

  .factory('User', ['$http', 'Config', function($http, Config) {

    var User = {

        //the request type
        type : 'users',

        userOBJ : {},

        username : "",

        password : "",

        setUsername : function(inputUsername) {
            User.username = inputUsername;
        },

        getUserName : function() {
            return this.username;
        },

        getLoggedUserName : function () {
            return localStorage.getItem("username");
        },

        setPassword : function(inputPassword) {
            User.password = inputPassword;
        },

        getPassword : function() {
            if (this.password != "") {
                return this.password;
            }
            else {
                return localStorage.getItem("password");
            }
        },

        storeUserLocally : function(obj) {
            //store it locally
            if (typeof(Storage) !== "undefined") {
                localStorage.setItem("username", this.getUserName());
                localStorage.setItem("password", this.getPassword());
            } else {
                // Sorry! No Web Storage support..
                console.log('no local storage available');
            }
        },

        getUserOBJ : function() {
            return $this.userOBJ;
        },

        //sends the username and password for authentication
        logIn : function(inputUsername, inputPassword, callback) {
            $http({
                method: 'GET',
                url: Config.url + this.type + '/login/' + inputUsername + '/' + inputPassword
            }).then(function successCallback(response) {
                User.setUsername(response.data.userId);
                User.setPassword(response.data.password);
                User.storeUserLocally(response.data);

                callback(response.status);
              }, function errorCallback(response) {
                // called asynchronously if an error occurs
                // or server returns response with an error status.
                callback(response.data.detail);
            });
        },

        register : function(inputUsername, inputPassword, callback) {
            var jsonObject = angular.toJson({"userId" : inputUsername, "password" : inputPassword});
            $http({
                method: 'POST',
                url: Config.url + this.type,
                data : jsonObject
            }).then(function successCallback(response) {
                callback(response.data);
              }, function errorCallback(response) {
                  callback(response);
            });
        },

        logout : function() {
            localStorage.removeItem('username');
            localStorage.removeItem('password');
        },

        getUserRating : function(userID, callback) {
            $http({
                method: 'GET',
                url: Config.url + this.type + '/avgrank/' + userID,
            }).then(function successCallback(response) {
                callback(response.data);
              }, function errorCallback(response) {
                  callback(response);
            });
        }

      }
      
      return User;
  }])

  /**
   * This is the Item factory / class
   */
  .factory('Item', ['$http', 'Config', function($http, Config) {

      var Item = { 

          /**
           * the thype of request
           */
          type : 'items',

          /**
           *  getAllItems 
           *  @return Array of items
           */
          getAllItems  : function(callback) {
              $http({
                method : 'GET',
                url : Config.url + this.type
              }).then(function successCallback(response) {
                callback(response.data);
              }).then(function errorCallback(response) {});
          },
          
          /**
           * getMyItems
           * @return Array
           */
           getMyItems : function(userId, callback) {
               $http({
                  method : 'GET',
                  url : Config.url + this.type + '/all/' + userId
              }).then(function successCallback(response) {
                  callback(response.data);
              }).then(function errorCallback(response) {
              });
            },

          /**
           * uploadItem 
           * @return bool
           */
          uploadItem : function(userName, itemName, itemDescription, itemPrice, itemImage, callback) {
              //prepare the object
              var newItem = {userId:userName, name:itemName, description:itemDescription, photo:itemImage, price:itemPrice};
              $http({
                  headers: {"User-Id" : userName},
                  method : 'POST',
                  url : Config.url + this.type,
                  data : newItem
              }).then(function successCallback(response) {
                  newItem['itemID'] = response.data.id;
                  callback(newItem);
              }).then(function errorCallback(response) {});
          },

          /**
           * uploadImageForItem - after the item is stored in the server
           * it returns the item id then we store the image for that item
           * @param String itemID - the ID of the item
           * @param File imageData this is the image of the item
           */
          uploadImageForItem : function(itemID, userName, imageData, callback) {
              //we will stream the image data
                var uploadSocket = new WebSocket("ws://" + location.host + "/api/itemsupload/" + itemID);
                //opend the socket and send the message
                uploadSocket.onopen = function(event) {
                    //console.log({src:imageData});
                    uploadSocket.send(imageData); 
                };
                //when the upload is successful
                uploadSocket.onmessage = function(event) {
                    console.log('the message is going .......');
                    console.log('closing the socket......');
                    callback({status:"success"});
                     //close the connection
                    uploadSocket.close();
                };
          },

          /**
           * downloadImageForItem - gets the image from the server for the specified item
           */
          downloadImageForItem : function(itemID, callback) {
                var downloadSocket = new WebSocket("ws://" + location.host + "/api/itemsdownload/" + itemID);
                //data comes in chunks so we have to concatinate it
                var data = "";

                downloadSocket.onmessage = function(event) {
                    data += event.data;
                };
                //when the message is complete all data chunks
                //are combined in the data variable
                downloadSocket.onclose = function(event) {
                    callback(data);
                }
          },

          /**
           * getItemWithID - returns the given Item
           * @param String itemID
           * @return Array item
           */
          getItemWithID : function(itemID, callback) {
              $http({
                  method : 'GET',
                  url : Config.url + this.type + '/' + itemID
              }).then(function successCallback(response) {
                  callback(response.data);
              }).then(function errorCallback(response) {
              });
          },

          /**
           * sellItemWithID
           */
          sellItemWithID : function(itemID, messageID, userName, callback) {
              $http({
                  headers: {"User-Id" : userName},
                  method : 'POST',
                  url : Config.url + 'rating/set/create',
                  data : {messageId : messageID, itemId : itemID, rating : 0}
              }).then(function successCallback(response) {
                 // console.log('this is the response from sell item With id ');
                  //console.log(response.data);
                  callback(response.data);
              }).then(function errorCallback(response) {
              });
          },

          /**
           * getItemStatus - checks if the item is sold
           */
          getItemStatus : function(item) {
              if (item.isSold != '' && item.isSold != '-1') {
                  //item is sold
                  return true;
              }
              else {
                  return false;
              }
          },

          getItemWithRatings : function(ratingID, callback) {
              Item.getRating(ratingID, function(rating) {
                  if (rating.rating == 0) {
                    var itemWithRanking = [];
                    itemWithRanking['ratingID'] = ratingID;
                    //still not rated yet
                    Item.getItemWithID(rating.itemId, function(item) {
                        itemWithRanking['item'] = item;
                        Item.downloadImageForItem(rating.itemId, function(imageData) {
                            itemWithRanking['item']['imageData'] = imageData;
                            callback(itemWithRanking);
                        });
                    });
                  }
                  else {
                      //console.log('this has been rated.....');
                      //console.log(rating);
                  }
              });
          },

          getRating : function(ratingID, callback) {         
              $http({
                  method : 'GET',
                  url : 'http://localhost:9000/api/rating/' + ratingID
              }).then(function successCallback(response) {
                  callback(response.data);
              }).then(function errorCallback(response) {});
          },

          rateThisItem : function(rate, ratingID, messageID, itemID, userName, callback) {
              //console.log('rate :'+rate+' ratingID:'+ratingID+' messageID:'+messageID+'itemID:'+itemID);
              $http({
                  headers: {"User-Id" : userName},
                  method : 'POST',
                  url : 'http://localhost:9000/api/rating/set/' + ratingID,
                  data : {rankingId: ratingID, messageId: messageID, itemId : itemID, rating : rate}
              }).then(function successCallback(response) {
                  callback(response.data);
              }).then(function errorCallback(response) {});
          }
      }

      return Item;
 }])
  
  /**
   * This is the Message factory / class
   */
  .factory('Message', ['$http', 'Config', function($http, Config){
    
    var Message = {

        /**
         * the type  
         */
        type : 'message',

        /**
         * @variable - holds all the messages for the specified item id
         */
        messages : [],

        /**
         * getMessagesForItemID - gets all the messages for the selected item
         */
        getMessagesForItemID : function (itemID, callback) {
            //console.log(Config.url + this.type + '/all/' + itemID);
            $http({
                method : 'GET',
                url : Config.url + this.type + '/all/' + itemID
            }).then(function successCallback(response) {
                Message.messages = response.data;
                callback(response.data);
            }).then(function errorCallback(error) {
                console.log(error);
            });
        },

        /**
         * getAllMessagesForUser
         */
        getAllMessagesForUser : function(userID, callback) {
            $http({
                method : 'GET',
                url : Config.url + this.type + '/all/by/' + userID
            }).then(function successCallback(response) {
                callback(response.data);
            }).then(function errorCallback(error) {
                console.log(error);
            });
        },

        /**
         * sendMessageForItemID
         */
        sendMessageForItemID : function(userID, itemID, message, callback) {
            //create the timestamp
            var date = new Date();
            var time = date.getTime();
            
            $http({
                headers: {"User-Id" : userID},
                method : 'POST',
                url : Config.url + this.type + '/send',
                data : {userId : userID, itemId : itemID, message : message, isSold : '', timestamp : time}
            }).then(function successCallback(response) {
                callback(response.data);
            }).then(function errorCallback(error) {
                console.log(error);
            });
        },

        /**
         * setMessageisSold - sets the message to sold
         */
        setMessageIsSold : function(messageID, callback) {
            angular.forEach(Message.messages, function(message) { 
                if (message.messageId == messageID) {
                    //console.log(message);
                    return callback(Message.messages);
                }
            });
        },

        getUserMessages : function(userID, callback) {
            $http({
                method : 'GET',
                url : Config.url + this.type + '/all/by/' + userID
            }).then(function successCallback(response) {
                callback(response.data);
            }).then(function errorCallback(error) {
                console.log(error);
            });
        },

        getMessagesOfItemsSold : function(msgs, callback) {
            var msgsSold = [];
            angular.forEach(msgs, function(msg) {
                 if (msg.isSold != "" && msg.isSold != '-1') {
                     msgsSold.push(msg);
                 }
            });

            return callback(msgsSold);
        }
    }

    return Message;
   }]);

});