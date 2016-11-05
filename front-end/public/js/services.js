/*global define */

'use strict';

define(['angular'], function(angular) {

/* Services */

// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('myApp.services', [])
  //value('version', '0.1')
  .factory('Config', [function(){
    var Config = {
        //this should be the hosting URL for NOW is localhost
        url : "http://localhost:9000/api/"
    };

    return Config;
}])

  .factory('User', ['$http', 'Config', function($http, Config){

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
                url: Config.url + this.type + '/' + 'login/' + inputUsername + '/' + inputPassword
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
        }

      }
      
      return User;
  }])

  /**
   * This is the Item factory / class
   */
  .factory('Item', ['$http', 'Config', function($http, Config){
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
              }).then(function errorCallback(response) {
              });
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
                  method : 'POST',
                  url : Config.url + this.type,
                  data : newItem
              }).then(function successCallback(response) {
                  callback(newItem);
              }).then(function errorCallback(response) {
              });
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
         * getMessagesForItemID
         */
        getMessagesForItemID : function (itemID, callback) {
            console.log(Config.url + this.type + '/all/' + itemID);
            $http({
                method : 'GET',
                url : Config.url + this.type + '/all/' + itemID
            }).then(function successCallback(response) {
                callback(response.data);
            }).then(function errorCallback(error) {
                console.log('this is the error ::');
                console.log(error);
            });
        },

        /**
         * sendMessageForItemID
         */
        sendMessageForItemID : function(userID, itemID, message, callback) {
            $http({
                method : 'POST',
                url : Config.url + this.type + '/send',
                data : {userId : userID, itemId : itemID, message : message, isSold : ''}
            }).then(function successCallback(response) {
                callback(response.data);
            }).then(function errorCallback(error) {
                console.log('this is what went wrong with your request');
                console.log(error);
            });
        }
    }

    return Message;
   }]);

});