package easycrypto;

import easycrypto.EasyCryptoAPI.Result;
import easycrypto.EasyCryptoAPI.ResultCode;

class Rot13Method implements CryptoMethod {

  @Override
  public Result encrypt(final String toEncrypt){
    String res="";
    char c='a';
    for (int i=0;i<toEncrypt.length();i++){
      c=toEncrypt.charAt(i);
      if (Character.isLowerCase(c)){
        c=((c<(int)'n') ? (c+=13) : (c-=13));
      } else{
        c=((c<(int)'N') ? (c+=13) : (c-=13));
      }
      res=res+c;
    }
    return new Result(ResultCode.ESuccess,res);
  }
  @Override
  public Result decrypt(final String toDecrypt){
    String res="";
    char c='a';
    for (int i=0;i<toDecrypt.length();i++){
      c=toDecrypt.charAt(i);
      if (Character.isLowerCase(c)){
        c=((c>=(int)'n') ? (c-=13) : (c+=13));
      } else{
        c=((c>=(int)'N') ? (c-=13) : (c+=13));
      }
      res=res+c;
    }
    return new Result(ResultCode.ESuccess,res);
  }
  @Override
  public String method(){
    return "rot13";
  }
}
