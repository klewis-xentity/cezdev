#---------------------------------------------------
# file: cllmconversationcontext.py
# desc: conversational context/token budget helper for llm prompts
#---------------------------------------------------

from .cllmcontext import CLLMContext

class CLLMConversationalContext (CLLMContext):
    def __init__(self):
        self.m_strassistantname = "Assistant"
        super().__init__()
    # end __init__()

    def create(self, strassistantname="Assistant", strcontext="", inumtokenbudget=CLLMContext.UNLIMITED_BUDGET):
        self.m_strassistantname = strassistantname
        self.m_ibudget = inumtokenbudget
        return self
    # end create()

    #---------------------------------
    # member access methods
    #---------------------------------
    def setAssistantName(self, strassistantname):
        self.m_strassistantname = strassistantname
        return self
    # end setAssistantName()

    def getAssistantName(self):
        return self.m_strassistantname
    # end getAssistantName()
    
    #---------------------------------
    # context management methods
    #---------------------------------
    def addAssistantContext(self, text):
        self.m_window.append(f"{self.m_strassistantname}: {text}")
        self._trimWindowToBudget()
        return self
    # end addAssistantContext()

    def buildContext(self, strprompt):
        self.addContext(strprompt)
        return super().buildContext()
    # end buildContext()

# end class CLLMConversationalContext
