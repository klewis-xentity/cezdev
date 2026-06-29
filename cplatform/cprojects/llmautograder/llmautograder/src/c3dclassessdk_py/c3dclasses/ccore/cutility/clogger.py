#---------------------------------------
# file: clogger.py
# desc: defines a logger object
#---------------------------------------
import logging

#---------------------------------------------------------
# name: CLogger
# desc: a logger class that wraps logging functionality
#---------------------------------------------------------
class CLogger:
    def __init__(self):
        self.m_logger = None
    # end __init__()
    
    def create(self,
               strlogid, 
               strlogformat='%(asctime)s - %(levelname)s - %(message)s', 
               strloglevel=logging.WARNING, 
               strlogfile="clogger.log"):
        self.m_logger = logging.getLogger(strlogid)
        self.m_logger.setLevel(strloglevel)
        self.m_logger.setLevel(logging.WARNING)
        
        
        # File handler for logging to a file
        if strlogfile:
            file_handler = logging.FileHandler(strlogfile)
            file_handler.setLevel(strloglevel)
            file_handler.setFormatter(logging.Formatter(strlogformat))
            self.m_logger.addHandler(file_handler)
        
        # Stream handler for logging to stdout
        stream_handler = logging.StreamHandler()
        stream_handler.setLevel(strloglevel)
        stream_handler.setFormatter(logging.Formatter(strlogformat))
        self.m_logger.addHandler(stream_handler)
    # end create()    
    
    def get(self):
        return self.m_logger
    # end get() 
    
    def __(self):
        return self.get()
    # end __()
# end CLogger

#--------------------------------------------------------
# name: getCLogger()
# desc: returns a clogger object
#--------------------------------------------------------
def getCLogger(strname):
    # setup the logger
    clogger = CLogger()
    clogger.create(strname)
    return clogger
# end initLogger()