'use strict';

const RANGE_ITERATOR = {
	get hasNext(){
		return this.current <= this.end;
	},

	get next(){
		// check this call will send the iterator out of bounds
		if(this.current > this.end){
			const message = 'Out of bounds'
			throw new IndexIteratorException(message);
		}

		return this.current++;
	}	
}

const SET_ITERATOR = {
	get hasNext(){
		return this.current <= this.set.length;
	},

	get next(){
		// check this call will send the iterator out of bounds
		if(this.current > this.set.length){
			const message = 'Out of bounds'
			throw new IndexIteratorException(message);
		}

		return this.set[this.current++];
	}
}

function IndexIterator(index){
	if(index.type === 'range'){
		this.type = 'range';
		this.start = index.start;
		this.end = index.end;
		this.current = this.start;
		Object.setPrototypeOf(this, RANGE_ITERATOR);
	}
	else if(index.type === 'set'){
		this.type = 'set';
		this.set = index.set;
		this.current = 0;
		Object.setPrototypeOf(this, SET_ITERATOR);
	}
	else{
		const message = 'Unexpected iterator type \'' + index.type + '\' received';
		throw new IndexIteratorException(message);
	}
}

function IndexIteratorException(message){
	this.message = message;
	this.location = location;
	this.toString = function(){
		return 'IndexIteratorException: ' + this.message;
	}
}