'use strict';

/**
 * A helper class for the interpreter which provides an iterator
 * over either a RangeNode or a SetNode.
 *
 * @public
 * @class
 * @property {!string} iterator - type of node iterator is iterating over
 * @property {!number} start (if type = 'range') - start point for iterator
 * @property {!number} end (if type = 'range') - end point for iterator
 * @property {!array} set (if type = 'set') - set of elements
 */
class IndexIterator {

	/**
	 * Constructs either a range or set instance of IndexInterpreter
	 * based on the specified index
	 *
	 * @public
	 * @param {!object} index - a range or set node to iterate over
	 */
	constructor(index){
		// constructs an iterator over a range
		if(index.type == 'range'){
			this.iterator = 'range';
			this.start = index.start;
			this.end = index.end;
			this.current = this.start;
		}
		// constructs an iterator over a set
		else if(index.type == 'set'){
			this.iterator = 'set';
			this.set = index.set;
			this.current = 0;
		}
	}

	/**
	 * Returns the type of this IndexIterator.
	 *
	 * @public
	 * @returns {!string} - iterator type
	 */
	get type(){
		return this.iterator;
	}

	/**
	 * Returns the next element in the iterator.
	 *
	 * @public
	 * @returns {!(number|string)} - the next element
	 */
	get next(){
		if(this.type == 'range'){
			return this._nextRangeElement;
		}
		else if(this.type == 'set'){
			return this._nextSetElement;
		}
	}

	/**
	 * Helper function for 'next' which returns the next
	 * range element.
	 *
	 * @private
	 * @returns {!number} - the next range element
	 */
	get _nextRangeElement(){
		// throw an exception if iterator has exceeded its bound
		if(!this._hasNextRangeElement){
			throw new IndexIteratorException('IndexIterator has exceeded its bounds.');
		}
		return this.current++;
	}

	/**
	 * Helper function for 'next' which returns the next
	 * set element.
	 *
	 * @private
	 * @returns {!string} - the next set element
	 */
	get _nextSetElement(){
		// throw an exception if iterator has exceeded its bound
		if(!this._hasNextSetElement){
			throw new IndexIteratorException('IndexIterator has exceeded its bounds.');
		}

		return this.set[this.current++];
	}

	/**
	 * Returns true if this iterator can return another element,
	 * otherwise returns false.
	 *
	 * @public
	 * @returns {!boolean} - whether iterator has next element or not
	 */
	get hasNext(){
		if(this.type == 'range'){
			return this._hasNextRangeElement;
		}
		else if(this.type == 'set'){
			return this._hasNextSetElement;
		}	
	}

	/**
	 * Helper function for 'hasNext' which returns true if this iterator
	 * can reutrn another range element, otherwise returns false.
	 *
	 * @private
	 * @returns {!boolean} - whether iterator has next range element or not
	 */
	get _hasNextRangeElement(){
		if(this.current <= this.end){
			return true;
		}

		return false;
	}

	/**
	 * Helper function for 'hasNext' which returns true if this iterator
	 * can reutrn another set element, otherwise returns false.
	 *
	 * @private
	 * @returns {!boolean} - whether iterator has next set element or not
	 */
	get _hasNextSetElement(){
		if(this.current < this.set.length){
			return true;
		}

		return false
	}
};

/**
 * An exception to be thrown by IndexInterpreter
 *
 * @public
 */
class IndexIteratorException {

	/**
	 * Constructs a new IndexInterpreterException.
	 *
	 * @public
	 * @param {!string} - the error message to display
	 */
	constructor(message){
		this.message = message;
	}
};