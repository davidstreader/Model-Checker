'use strict';

function IndexIterator(index){
	var iterator = {};

	// determine whether a range or a set is to be iterated
	if(index.range.type === 'range'){
		setupRangeIterator(index.range);
	}
	else if(index.range.type === 'set'){
		setupSetIterator(index.range);
	}
	else{
		// throw error
	}

	iterator.variable = index.variable;
	return iterator;

	function setupRangeIterator(index){
		iterator.type = 'range';
		iterator.start = index.start;
		iterator.end = index.end;
		iterator.current = iterator.start;

		iterator.hasNext = function(){
			return iterator.current <= iterator.end;
		}

		iterator.next = function(){
			if(iterator.current > iterator.end){
				// throw error
			}

			return iterator.current++;
		}
	}

	function setupSetIterator(index){
		iterator.type = 'set';
		iterator.set = index.set;
		iterator.current = 0;

		iterator.hasNext = function(){
			return iterator.current < iterator.set.length;
		}

		iterator.next = function(){
			if(iterator.current >= iterator.set.length){
				// throw error
			}

			return iterator.set[iterator.current++];
		}
	}
}