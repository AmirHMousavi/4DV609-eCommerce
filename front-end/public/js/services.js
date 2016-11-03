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
                url: Config.url + this.type + '/' + inputUsername + '/' + inputPassword
            }).then(function successCallback(response) {
                User.setUsername(response.data.userId);
                User.setPassword(response.data.password);

                User.storeUserLocally(response.data);

                callback(response.status);
              }, function errorCallback(response) {
                // called asynchronously if an error occurs
                // or server returns response with an error status.
                console.log(response);
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
                console.log(response);
                callback(response.data);
              }, function errorCallback(response) {
                  console.log(response);
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
                  console.log('this is the error ::' + response);
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
                  console.log('this is the error ::' + response);
              });
            },

          /**
           * uploadItem 
           * @return bool
           */
          uploadItem : function(userName, itemName, itemDescription, itemPrice, itemImage, callback) {
              //prepare the object
              var newItem = {userId:userName, name:itemName, description:itemDescription, photo:itemImage, price:itemPrice};
              console.log('this is the new item to be added');
              console.log(newItem);
              $http({
                  method : 'POST',
                  url : Config.url + this.type,
                  data : newItem
              }).then(function successCallback(response) {
                  console.log('this is the response of ADDING a new item ::');
                  console.log(response);
                  callback(newItem);
              }).then(function errorCallback(response) {
                  console.log('this is the error ::' + response);
              });
          }
      }

      return Item;
  }]);


});