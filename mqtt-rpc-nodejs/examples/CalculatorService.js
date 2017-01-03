/*******************************************************************************
 *******************************************************************************/
var debug = require('debug')('calculator');

var CalculatorService = function(){
    
    var self = this;


    this.add = function(a,b){
	debug('add',a,b);
	return a + b;
    };

    this.sub = function(a,b){
	debug('sub',a,b);
	return a - b;
    }
};

module.exports = CalculatorService;
