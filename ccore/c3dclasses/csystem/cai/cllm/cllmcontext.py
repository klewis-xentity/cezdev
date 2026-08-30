#---------------------------------------------------
# file: cllmcontext.py
# desc: context/token budget helper for llm prompts
#---------------------------------------------------

class CLLMContext:

    #-----------------------------------------
    # constants
    #-----------------------------------------
    UNLIMITED_BUDGET = -1
    m_strglobalcontext = ""

    #-----------------------------------------
    # constructor
    #-----------------------------------------
    def __init__(self):
        self.m_strname = ""
        self.m_ibudget = CLLMContext.UNLIMITED_BUDGET
        self.m_window = []
    # end __init__()

    #------------------------------------
    # creation and management method
    #------------------------------------
    def create(self, strname, icontextbudget=None):
        if icontextbudget is None:
            icontextbudget = CLLMContext.UNLIMITED_BUDGET
        # end if
        self.m_ibudget = icontextbudget
        self.m_strname = strname
        return self
    # end create()

    #----------------------------------
    # member access methods
    #----------------------------------
    def setName(self, strname):
        self.m_strname = strname
        return self
    # end setName()

    def getName(self):
        return self.m_strname
    # end getName()

    def setBudget(self, icontextbudget):
        self.m_ibudget = icontextbudget
        self._trimWindowToBudget()
        return self
    # end setBudget()

    def getBudget(self):
        return self.m_ibudget
    # end getBudget()

    def setContextWindow(self, contextwindow):
        self.m_window = contextwindow
        self._trimWindowToBudget()
        return self
    # end setContextWindow()

    def getContextWindow(self):
        return self.m_window
    # end getContext()

    @staticmethod
    def setGlobalContext(strglobalcontext):
        CLLMContext.m_strglobalcontext = strglobalcontext or ""
        return CLLMContext.m_strglobalcontext
    # end setGlobalContext()

    @staticmethod
    def getGlobalContext():
        return CLLMContext.m_strglobalcontext
    # end getGlobalContext()

    @staticmethod
    def clearGlobalContext():
        CLLMContext.m_strglobalcontext = ""
        return CLLMContext.m_strglobalcontext
    # end clearGlobalContext()

    @staticmethod
    def toStringGlobalContext():
        strglobalcontext = CLLMContext.getGlobalContext()
        itokens = CLLMContext._getTokenSize(strglobalcontext)
        ientries = 1 if strglobalcontext.strip() else 0
        strwindowpreview = CLLMContext._getTokenPreview(strglobalcontext)
        return "\n".join(
            [
                "GlobalContext:",
                "  name: GlobalContext",
                f"  budget: {CLLMContext.UNLIMITED_BUDGET}",
                f"  tokens: {itokens}",
                f"  entries: {ientries}",
                "  full: False",
                f"  window: {strwindowpreview}",
            ]
        )
    # end toStringGlobalContext()

    #----------------------------------
    # context management methods
    #----------------------------------
    def addContext(self, contexttext):
        self.m_window.append(f"{self.m_strname}: {contexttext}")
        self._trimWindowToBudget()
        return self
    # end setContext()
    
    def clearContext(self):
        self.m_window = []
        return self
    # end clearContext()

    def isContextEmpty(self, contextname):
        return len(self.m_window) == 0
    # end isContextEmpty()

    def isContextFull(self, contextname):
        if self.m_ibudget == CLLMContext.UNLIMITED_BUDGET:
            return False
        # end if
        if self.m_ibudget <= 0:
            return self._getWindowTokenSize() > 0
        # end if
        return self._getWindowTokenSize() >= self.m_ibudget
    # end isFullContext()

    #---------------------------------
    # context building methods
    #---------------------------------
    def buildContext(self):
        # assemble the full context window for a memory-enabled prompt
        return "\n".join(self.m_window)
    # end buildContext()

    def toString(self):
        strwindowpreview = CLLMContext._getTokenPreview("\n".join(str(item) for item in self.m_window))
        return "\n".join(
            [
                "CLLMContext:",
                f"  name: {self.m_strname}",
                f"  budget: {self.m_ibudget}",
                f"  tokens: {self._getWindowTokenSize()}",
                f"  entries: {len(self.m_window)}",
                f"  full: {self.isContextFull(self.m_strname)}",
                f"  window: {strwindowpreview}",
            ]
        )
    # end toString()

    def __str__(self):
        return self.toString()
    # end __str__()

    #---------------------------------
    # private methods
    #---------------------------------
    def _trimWindowToBudget(self):
        if self.m_ibudget == CLLMContext.UNLIMITED_BUDGET:
            return
        # end if
        window_size_before = len(self.m_window)
        tokens_before = self._getWindowTokenSize()
        if self.m_ibudget <= 0:
            if window_size_before > 0:
                print(f"CLLMContext trim: cleared {window_size_before} entries because budget={self.m_ibudget}.")
            # end if
            self.m_window = []
            return
        # end if
        removed_count = 0
        while self._getWindowTokenSize() > self.m_ibudget:
            self.m_window.pop(0)
            removed_count += 1
        # end while
        if removed_count > 0:
            tokens_after = self._getWindowTokenSize()
            print(
                "CLLMContext trim: removed "
                f"{removed_count} entries to fit budget={self.m_ibudget} "
                f"(tokens {tokens_before} -> {tokens_after})."
            )
        # end if
    # end _trimWindowToBudget()

    def _getWindowTokenSize(self):
        return sum(CLLMContext._getTokenSize(item) for item in self.m_window)
    # end _getWindowTokenSize()

    @staticmethod
    def _getTokenSize(item):
        return len(str(item).split())
    # end _getTokenSize()

    @staticmethod
    def _getTokenPreview(item, ihead=10, itail=3):
        stritem = str(item).strip()
        if not stritem:
            return ""
        # end if
        tokens = stritem.split()
        if len(tokens) <= (ihead + itail):
            return " ".join(tokens)
        # end if
        return " ".join(tokens[:ihead]) + " ..... " + " ".join(tokens[-itail:])
    # end _getTokenPreview()
# end CLLMContext


