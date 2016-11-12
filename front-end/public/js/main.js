/*global require, requirejs */

'use strict';

requirejs.config({
  paths: {
      'angular': ['../lib/angularjs/angular'],
      'angular-route': ['../lib/angularjs/angular-route'],
      'angular-animate' : ['../lib/angularjs/angular-animate'],
      'angular-aria' : ['../lib/angularjs/angular-aria.min'],
      'jquery' : ['https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min'],
      'angular-material' : ['../lib/angular-material/angular-material.min', /*'http://ajax.googleapis.com/ajax/libs/angular_material/1.1.0/angular-material.min'*/],
      'angular-material-icons' : [/*'../lib/angularjs/angular-material-icons.min'*/'http://cdnjs.cloudflare.com/ajax/libs/angular-material-icons/0.7.1/angular-material-icons.min']
  },
  shim: {
      'angular': {
        exports : 'angular'
      },
      'angular-route': {
        deps: ['angular'],
        exports : 'angular'
      },
      'angular-animate': {
          deps: ['angular']
      },
      'angular-aria': {
          deps: ['angular']
      },
      'angular-material': {
         deps: ['angular-animate', 'angular-aria']
      },
      'angular-material-icons': {
          deps: ['angular-material'],
          exports : 'angular-material'
      }
  }
});

require(['angular', './controllers', './directives', './filters', './services', 'angular-route','angular-animate', 'angular-aria',
        'angular-material', 'jquery', 'angular-material-icons'],
  function(angular, controllers) {

    // Declare app level module which depends on filters, and services

    angular.module('myApp', ['myApp.filters', 'myApp.services', 'myApp.directives', 'ngRoute', 'ngMaterial','ngMdIcons']).
        config(['$routeProvider','$mdThemingProvider', function($routeProvider, $mdThemingProvider) {
             $mdThemingProvider.theme('default')
                .primaryPalette('red')
                .accentPalette('deep-purple');

        $routeProvider.when('/view1', {templateUrl: 'partials/partial1.html', controller: controllers.MyCtrl1});
        $routeProvider.when('/view2', {templateUrl: 'partials/partial2.html', controller: controllers.MyCtrl2});
        $routeProvider.when('/login', {templateUrl: 'partials/login.html', controller: controllers.LoginCtrl});
        $routeProvider.when('/register', {templateUrl: 'partials/login.html', controller: controllers.LoginCtrl});
        $routeProvider.when('/items', {templateUrl: 'partials/items.html', controller: controllers.ItemsCtrl});
        $routeProvider.when('/account', {templateUrl: 'partials/account.html', controller: controllers.AccountCtrl});
        $routeProvider.when('/documentation', {templateUrl: 'partials/documentation.html', controller: controllers.DocumentationCtrl});
        $routeProvider.when('/us', {templateUrl: 'partials/us.html', controller: controllers.UsCtrl});
        $routeProvider.otherwise({redirectTo: '/items'});
      }])
      .controller('mainController', ['$scope','$rootScope','$q','$location','$mdToast','$mdDialog','User', 'Item', 'Message',
        function($scope, $rootScope, $q, $location, $mdToast, $mdDialog, User, Item, Message){
            $rootScope.currentUser = "";
            $rootScope.isLoggedIn = false;
            angular.element(document).ready(function () {
                if (User.getLoggedUserName() != undefined && User.getLoggedUserName != null) {
                    //we have a logged user
                    $rootScope.currentUser = User.getLoggedUserName();
                    $rootScope.isLoggedIn = true;
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
                            else {
                               //alert('no rating...');
                            }
                        });
                    });
                }
                else {
                    $rootScope.currentUser = "";
                    $rootScope.isLoggedIn = false;
                }
            });

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

            //logout triggered
            $scope.logMeOut = function() {
                ///log user out
                User.logout();
                $rootScope.currentUser = "";
                $rootScope.isLoggedIn = false;
                document.location = '#/';
            }
      }]);

    angular.bootstrap(document, ['myApp']);

});

function logMeIn() {
    window.location.href = '#/login'
}

function goTo(location) {
    window.location.href = location;
}
