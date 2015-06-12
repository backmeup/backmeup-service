package org.backmeup.model;


public class Token {
  private String token;
  
  // the next backup date to use; leave null, if the token is not reusable
  private Long ttl;
  
  public Token(String token, Long backupdate) {
    super();
    this.token = token;
    this.ttl = backupdate;
  }
  
  public Token() {
      
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Long getTtl() {
    return ttl;
  }

  public void setTtl(Long ttl) {
    this.ttl = ttl;
  }
}
