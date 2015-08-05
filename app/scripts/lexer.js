// class Lexer
function Lexer(ltsString) {

    // the labeled transfer string we will be parsing
    this.ltsString = ltsString;
    //initialise the index at 0
    this.index = 0;
    //initialise the token types the lexer understands
    this.TOKEN_TYPE = {
        DEFINE: '=',
        LBRACK: '\\(',
        RBRACK: '\\)',
        LSQBRACK: '\\[',
        RSQBRACK: '\\]',
        SEQUENCE: '->',
        ZERO_MANY: '\\*',
        ONE_MANY: '\\+',
        OR: '\\|',
        PARALLEL: '\\|\\|',
        OPTIONAL: '\\?',
        ENDFILE: 'EOF',
        END: '\\.',
        EMPTY: 'ε',
        UNKNOWN: 'unknown',
        LABEL: '[a-zA-Z]+'
    };

    //these are the tokens which should not be parsed as input, only ouput
    // i.e. "EOF" should be parsed as label, not endfile
    this.PRIVATE_TOKENS = ["ENDFILE", "EMPTY", "UNKNOWN"];

};


//Check that the lexer has more to parse
Lexer.prototype.hasNext = function () {
    //check that the ltsString is set
    if (this.ltsString) {
        //check the current index against the length of the ltsString
        return this.index < this.ltsString.length;
    }
    return false;
}

//Return the next token and do not move the index if found, return an error if not valid
Lexer.prototype.peek = function () {

    //get the next token
    var token = this._nextToken();

    //simply return the token do not consume
    return token;
}

//Return the next token and move the index if found, return an error if not valid
Lexer.prototype.next = function () {

    //get the next token
    var token = this._nextToken();

    //check that we have found a valid token
    if (token.type == 'ENDFILE') {
      //simply return the token do not consume
      return token;
    } else {
        //consume the token text, moving the index
        this._consume(token.text);
        //return the token we found
        return token;
    }
}

//Return the next token (only consuming whitespace) or return error
Lexer.prototype._nextToken = function () {

    // var temporaryIndex = this.index;

    // while there is stuff to parse, keep looking to return the next token
    while (this.hasNext()) {

        //get the string beginning at the current index
        var ltsStringAtIndex = this.ltsString.substring(this.index);

        //initialise the token to be returned with null
        var currentToken = null;

        // if the character at the start of the string is matched as whitespace
        if(/^\s/.test(ltsStringAtIndex)){
          // consume the character at the beginning of the string
          var ltsStringStart = ltsStringAtIndex.substring(0, 1);
          //temporaryIndex = temporaryIndex + ltsStringStart.length;
          this._consume(ltsStringStart);

          //go to the beginning of the loop
          continue;
        }

        //for each token type defined, check for matches and return the match whose token type is the longest
        for (type in this.TOKEN_TYPE) {

            //constant tokens should not be evaluated
            if(_.contains(this.PRIVATE_TOKENS, type)){
              continue;
            }

            //get the token e.g. '+'
            var token = this.TOKEN_TYPE[type];

            //check if the start of the current string matches the token
            if (this._isMatch(token, ltsStringAtIndex)) {

                //get the matched content
                var matchedTokenContent = this._getFirstMatch(token, ltsStringAtIndex)

                if (currentToken === null) {
                    //if the current token is null, set it for the first time

                    currentToken = new this._Token(type, matchedTokenContent);


                } else {
                    //currentToken is already set, so we need to check if the token currently being evaluated is longer than the 'currentToken'
                    // this ensures that we are matching/returning the token that is hardest to satisfy

                    if (type.length > currentToken.type.length) {
                        //occurs when currentToken.type = '|' and the token being evaluated is '||'
                        currentToken = new this._Token(type, matchedTokenContent);

                    } else {
                        // ignore tokens that are easier to satisfy
                    }

                }


            }

        }

        //check that we have found a valid token
        if (currentToken === null) {
            //no matches found
            throw new Error('Unknown type of ' + ltsStringAtIndex);
        } else {
            //return the token we found
            return currentToken;
        }

    }

    //no more content to parse, so we are at the end
    return new this._Token('ENDFILE', this.TOKEN_TYPE.ENDFILE);
}

// class Token
Lexer.prototype._Token = function (type, text) {
    //the type of the token e.g. 'ONE_OR_MANY'
    this.type = type;
    //the text of the token e.g. '+'
    this.text = text;
}

Lexer.prototype._isLetterOrDigit = function (regChar) {
    //check that the char passed is a letter or digit
    return (regChar >= 'a' && regChar <= 'z') ||
        (regChar >= 'A' && regChar <= 'Z') ||
        (regChar >= '0' && regChar <= '9');
}

Lexer.prototype._newRegex = function (regexContent) {
    //create a regex that checks for the content from the beginning
    return new RegExp('^' + regexContent, 'g');
}

Lexer.prototype._isMatch = function (regexContent, stringToCheck) {
    //create a new regex that checks the 'regexContent' is at the beginning of the 'stringToCheck'
    var regexp = this._newRegex(regexContent);
    return regexp.test(stringToCheck);
}

Lexer.prototype._getFirstMatch = function (regexContent, stringToMatch) {
    //create a new regex that matches the 'regexContent' at the beginning of the 'stringToMatch'
    var regexp = this._newRegex(regexContent);
    var matches = regexp.exec(stringToMatch);
    //ASSUMPTION, isMatch is called and is successful before this
    var firstMatch = matches[0];
    return firstMatch;
}

Lexer.prototype._consume = function (content) {
    //move the lexer's index
    this.index = this.index + content.length;
    return this.index;
}
