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
      url : "http://localhost:9000/api/"
    };

    return Config;

  }])

  .factory('User', ['$http', 'Config', function($http, Config){

      var User = {

        //the request type
        type : 'users',

        username : "",

        password : "",

        setUsername : function(inputUsername) {
            User.username = inputUsername;
        },

        getUserName : function() {
            return this.username;
        },

        setPassword : function(inputPassword) {
            User.password = inputPassword;
        },

        getPassword : function() {
            return this.password;
        },

        //sends the username and password for authentication
        logIn : function(inputUsername, inputPassword, callback) {
            $http({
                method: 'GET',
                url: Config.url + this.type + '/' + inputUsername + '/' + inputPassword
            }).then(function successCallback(response) {
                User.setUsername(response.data.userId);
                User.setPassword(response.data.password);
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
              }, function errorCallback(response) {
                  console.log(response);
            });
        }

      }

      return User;
  }]);


});