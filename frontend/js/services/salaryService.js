/**
 * Salary Service
 * Handles all HTTP interactions with /api/v1/employees/{employeeId}/salaries
 */

(function () {
  'use strict';

  angular.module('employeeApp')
    .factory('SalaryService', ['$http', 'apiConfig', function ($http, apiConfig) {

      function getBaseUrl(employeeId) {
        return apiConfig.baseUrl + '/employees/' + employeeId + '/salaries';
      }

      return {
        getSalaryHistory: function (employeeId) {
          return $http.get(getBaseUrl(employeeId));
        },

        addSalary: function (employeeId, salaryData) {
          var payload = {
            amount: parseFloat(salaryData.amount),
            currency: (salaryData.currency || 'USD').toUpperCase().trim(),
            effectiveDate: salaryData.effectiveDate
          };
          return $http.post(getBaseUrl(employeeId), payload);
        }
      };
    }]);
})();
