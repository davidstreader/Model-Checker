/**
 * An Exception class.
 *
 * @public
 * @class
 * @param {Array} message The error message
 */
function InterpreterException(message) {
  this.message = message;
}

InterpreterException.prototype.toString = function() {
  return 'InterpreterException: ' + this.message;
};
