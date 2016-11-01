/*global define */

'use strict';

define(function() {

/* Controllers */

var controllers = {};

controllers.MyCtrl1 = function($scope) {

    $scope.login = function() {
        alert('this is scope speaking');
    }
}
controllers.MyCtrl1.$inject = ['$scope'];

controllers.MyCtrl2 = function() {

}
controllers.MyCtrl2.$inject = [];

return controllers;

});