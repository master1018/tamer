public class POSIXSignals {
  private static String[] signalNames = {
    "",           
    "SIGHUP",     
    "SIGINT",     
    "SIGQUIT",    
    "SIGILL",     
    "SIGTRAP",    
    "SIGABRT",    
    "SIGEMT",     
    "SIGFPE",     
    "SIGKILL",    
    "SIGBUS",     
    "SIGSEGV",    
    "SIGSYS",     
    "SIGPIPE",    
    "SIGALRM",    
    "SIGTERM",    
    "SIGUSR1",    
    "SIGUSR2",    
    "SIGCHLD",    
    "SIGPWR",     
    "SIGWINCH",   
    "SIGURG",     
    "SIGPOLL",    
    "SIGSTOP",    
    "SIGTSTP",    
    "SIGCONT",    
    "SIGTTIN",    
    "SIGTTOU",    
    "SIGVTALRM",  
    "SIGPROF",    
    "SIGXCPU",    
    "SIGXFSZ",    
    "SIGWAITING", 
    "SIGLWP",     
    "SIGFREEZE",  
    "SIGTHAW",    
    "SIGCANCEL",  
    "SIGLOST",    
  };
  public static String getSignalName(int sigNum) {
    if ((sigNum <= 0) || (sigNum >= signalNames.length)) {
      return "<Error: Illegal signal number " + sigNum + ">";
    }
    return signalNames[sigNum];
  }
}
