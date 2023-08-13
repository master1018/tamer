public class LinuxSignals {
  private static String[] signalNames = {
    "",           
    "SIGHUP",     
    "SIGINT",     
    "SIGQUIT",    
    "SIGILL",     
    "SIGTRAP",    
    "SIGABRT",    
    "SIGIOT",
    "SIGBUS",
    "SIGFPE",     
    "SIGKILL",    
    "SIGUSR1",    
    "SIGSEGV",    
    "SIGUSR2",    
    "SIGPIPE",    
    "SIGALRM",    
    "SIGTERM",    
    "SIGSTKFLT",
    "SIGCHLD",    
    "SIGCONT",    
    "SIGSTOP",    
    "SIGTSTP",    
    "SIGTTIN",    
    "SIGTTOU",    
    "SIGURG",     
    "SIGXCPU",    
    "SIGXFSZ",    
    "SIGVTALRM",  
    "SIGPROF",    
    "SIGWINCH",   
    "SIGPOLL",    
    "SIGPWR",     
    "SIGSYS"
  };
  public static String getSignalName(int sigNum) {
    if ((sigNum <= 0) || (sigNum >= signalNames.length)) {
      return "<Error: Illegal signal number " + sigNum + ">";
    }
    return signalNames[sigNum];
  }
}
