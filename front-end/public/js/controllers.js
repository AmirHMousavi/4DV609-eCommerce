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

controllers.LoginCtrl = function($scope, $rootScope, Config, User) 
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
                alert(response);
            });
        }
        else {
            return;
        }
    }

}

controllers.LoginCtrl.$inject = ['$scope', '$rootScope', 'Config','User'];

controllers.ItemsCtrl = function($scope, Item) 
{
    $scope.items = {};
    //this is what is loaded when the Items page is ready
    //we get all the items
    angular.element(document).ready(function () {
        Item.getAllItems(function(response){
            $scope.items = response;
            console.log($scope.items);
        });
        
    });
    
}

controllers.ItemsCtrl.$inject = ['$scope', 'Item'];

controllers.AccountCtrl = function($scope, $rootScope, User, Item, $mdToast) 
{
    $scope.isLoggedIn = false;

    $scope.myItems = [];
    $scope.showAccountPart = 'profile';

    $scope.username = User.getUserName();
    $scope.password = User.getPassword();

    $scope.itemName = "";
    $scope.itemDescription = "";
    $scope.itemPrice = "";

    $scope.changeAccountView = function(view) {
        $scope.showAccountPart = view;
    };

    //triggered on page load
    angular.element(document).ready(function () {
        if ($rootScope.currentUser !== undefined && $rootScope.currentUser !== '') {
            Item.getMyItems($scope.username, function(response) {
                $scope.myItems = response;
            });
            $scope.isLoggedIn = true;
             //alert('is logged in :: ' + $scope.isLoggedIn + " current user :" + $rootScope.currentUser);
        }
        else {
            //alert('not logged in');
            //they are not supposed to see this page
            //document.location = "#/";
            $scope.isLoggedIn = false;
        }
        
    });

    //triggered when we want to upload a new item
    $scope.uploadItem = function() {
        Item.uploadItem($scope.username, $scope.itemName, $scope.itemDescription, $scope.itemPrice, 'photo', function(newItem) {
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
}

controllers.AccountCtrl.$inject = ['$scope', '$rootScope', 'User', 'Item', '$mdToast'];

return controllers;

});