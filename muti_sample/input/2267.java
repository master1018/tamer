class TokenBuffer
{
  private final int DEFAULT_SIZE = 10;
  private int   _size      = 0;
  private Token _buffer [] = null;
  private int   _currPos   = -1;
  TokenBuffer ()
  {
    _size    = DEFAULT_SIZE;
    _buffer  = new Token [_size];
    _currPos = -1;
  } 
  TokenBuffer (int size) throws Exception
  {
    _size    = size;   
    _buffer  = new Token [_size];
    _currPos = -1;
  } 
  void insert (Token token)
  {
    _currPos = ++_currPos % _size;
    _buffer [_currPos] = token;
  }
  Token lookBack (int i)
  {
    return _buffer [(_currPos - i) >= 0 ? _currPos - i : _currPos - i + _size];
  }
  Token current ()
  {
    return _buffer [_currPos];
  }
}   
