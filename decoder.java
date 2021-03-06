/*

  decoder.java
  written by Colin Hardy : cdh.cert@gmail.com
  10.01.2017
  
  compile:
  javac decoder.java

  usage:
  java decoder filename

  overview:
  this code is designed to de-obfuscate malicious .jar files often 
  used in banking trojan malware. It requires the user to supply a file which 
  is the decompiled class file output, from using a tool such as jd-gui. It
  finds the DES key used to obfuscate strings by using some regex kung-fu.
  example - java decoder malware.java

*/

import java.io.*;
import java.util.regex.*;
import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class decoder {

  /*
    returns the byte output of the passed input string
  */
  private static byte[] byteFunc(String strInput) {
    byte[] myByte = new byte[strInput.length() / 2];
    for (int i = 0; i < myByte.length; i++) {
        String myStr = strInput.substring(2 * i, 2 * i + 2);
        int myInt = Integer.parseInt(myStr, 16);
        myByte[i] = ((byte) myInt);
    }
    return myByte;
  }

  /*
    return the decrypted output of the passed input string.
    this uses the 8-char key provided by via CLA.
  */
  public static String cryptoFunc(String strInput, String key)
  throws Exception {
    byte[] newByte = byteFunc(strInput);
    Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
    DESKeySpec desKey = new DESKeySpec(key.getBytes("UTF-8"));
    SecretKeyFactory secretKeyFact = SecretKeyFactory.getInstance("DES");
    SecretKey secretKey = secretKeyFact.generateSecret(desKey);
    IvParameterSpec ivParameter = new IvParameterSpec(key.getBytes("UTF-8"));
    cipher.init(2, secretKey, ivParameter);
    byte[] tmpByte = cipher.doFinal(newByte);
    return new String(tmpByte);
  }

  /*
    parse the input file to extract the DES key.
  */
  public static String fileParse(File inFile, Pattern p) 
  throws FileNotFoundException {
    FileInputStream inputStream = new FileInputStream(inFile);
    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
    String strLine;
    String match = "";

    try {
        while ((strLine = br.readLine()) != null) {
            Matcher m = p.matcher(strLine);
            while (m.find()) {
                match = m.group(1);
            }
        }
        br.close();
    } catch (Exception ex) {
        ex.printStackTrace(System.out);
    } 
    return match;
  }

  /*
    supply the patterns to use in the fileParse method to extract the key
  */
  public static String getKey(File inFile) 
  throws FileNotFoundException {
    String tmp = "";
    Pattern p = Pattern.compile("DESKeySpec\\((.*)\\.");
    String keyVar = fileParse(inFile, p);
    tmp = Pattern.quote(keyVar);
    Pattern q = Pattern.compile(tmp + " *?= *?\"(.*)\";");
    String key = fileParse(inFile, q);
    System.out.println(key);
    return key;
  }

  /*
    main
    searches the input file for strings between quotes.
    with each match, passes the output to the cryptofunc in order to decrypt
    using the 8-char key provided. 
    prints output to stdout.
  */
  public static void main(String[] args)
  throws Exception {
      // ensure correct usage
      File inFile = null; 
      if (args.length == 1) {
          try {
              inFile = new File(args[0]);
              if (!inFile.exists()) {
                  System.err.println("Error: the specified file " + inFile + " cannot be found.");
                  System.exit(1);
              }
          } catch (Exception ex) {
              ex.printStackTrace(System.out);
          }
      } else {
          System.err.println("Usage: java decoder <filename>");
          System.exit(1);
      }

      // get the key
      String key = getKey(inFile);
      
      /*
        search the input file for strings between quotes.
        pass each match to the decrypt function, assuming it meets simple DES-based requirements.
      */
      FileInputStream inputStream = new FileInputStream(inFile);
      BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
      String strLine;
      try {
        while ((strLine = br.readLine()) != null) {
          Pattern pat = Pattern.compile("\"([^\"]*)\"");
          Matcher mat = pat.matcher(strLine);
          while (mat.find()) {
            if ( (mat.group(1).length() % 8) != 0 || mat.group(1).length() < 9) {
              // skip over lines that don't meet the criteria. 
            } else {
              String output = cryptoFunc(mat.group(1), key);
              System.out.println(output);
            }
          }
        } 
        br.close();
      } catch (Exception ex) {
        ex.printStackTrace(System.out);
      } 

    }
  }