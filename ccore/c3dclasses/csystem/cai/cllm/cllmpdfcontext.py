#---------------------------------------------------
# file: cllmpdfcontext.py
# desc: pdf context/token budget helper for llm prompts.
#       loads a pdf and partitions its text into
#       context-budget-sized pages. call set(n) to
#       load page n into the active context window.
#---------------------------------------------------

from .cllmcontext import CLLMContext

class CLLMPDFContext(CLLMContext):

    #-----------------------------------------
    # constructor
    #-----------------------------------------
    def __init__(self):
        super().__init__()
        self.m_strpdfpath = ""
        self.m_pages = []       # list of str, one entry per budget-sized page
        self.m_icurrentpage = -1
    # end __init__()

    #------------------------------------
    # creation method
    #------------------------------------
    def create(self, strpdfpath, icontextbudget=CLLMContext.UNLIMITED_BUDGET):
        self.m_strpdfpath = strpdfpath
        self.m_ibudget = icontextbudget
        self.m_pages = []
        self.m_icurrentpage = -1
        self._loadPDF()
        return self
    # end create()

    #----------------------------------
    # page navigation
    #----------------------------------
    def set(self, ipageindex):
        """Load page ipageindex into the active context window."""
        if not self.m_pages:
            self.m_window = []
            return self
        # end if
        ipageindex = max(0, min(ipageindex, len(self.m_pages) - 1))
        self.m_icurrentpage = ipageindex
        self.m_window = [self.m_pages[ipageindex]]
        return self
    # end set()

    #----------------------------------
    # member access
    #----------------------------------
    def getPageCount(self):
        return len(self.m_pages)
    # end getPageCount()

    def getCurrentPage(self):
        return self.m_icurrentpage
    # end getCurrentPage()

    def getPDFPath(self):
        return self.m_strpdfpath
    # end getPDFPath()

    #----------------------------------
    # private helpers
    #----------------------------------
    def _loadPDF(self):
        """Read the PDF and chunk its text into budget-sized pages."""
        strtext = self._readPDFText()
        if not strtext or not strtext.strip():
            return
        # end if
        self.m_pages = self._chunkText(strtext)
    # end _loadPDF()

    def _readPDFText(self):
        strtext = ""
        try:
            from pypdf import PdfReader
            with open(self.m_strpdfpath, "rb") as infile:
                reader = PdfReader(infile)
                for page in reader.pages:
                    page_text = page.extract_text() or ""
                    if page_text.strip():
                        strtext += page_text + "\n\n"
                    # end if
                # end for
            # end with
        except Exception as e:
            print(f"CLLMPDFContext: error reading PDF '{self.m_strpdfpath}': {e}")
        # end try
        return strtext
    # end _readPDFText()

    def _chunkText(self, strtext):
        """Split strtext into budget-sized chunks.

        When no budget is set (UNLIMITED_BUDGET) the entire text is one page.
        Budget is treated as a token count; 1 token ~= 4 UTF-8 bytes.
        """
        if self.m_ibudget == CLLMContext.UNLIMITED_BUDGET or self.m_ibudget <= 0:
            return [strtext.strip()]
        # end if

        # Reserve headroom for prompt scaffolding and model output.
        available_tokens = max(int(self.m_ibudget * 0.45), 500)
        max_bytes = max(available_tokens * 4, 2000)

        lines = [ln.strip() for ln in strtext.replace("\r\n", "\n").split("\n") if ln.strip()]
        chunks = []
        current = ""

        for line in lines:
            candidate = f"{current}\n\n{line}".strip() if current else line
            if current and len(candidate.encode("utf-8")) > max_bytes:
                chunks.append(current)
                current = line
                # Fallback: split an overlong single line by words.
                while len(current.encode("utf-8")) > max_bytes:
                    words = current.split()
                    temp = ""
                    idx = 0
                    while idx < len(words):
                        next_temp = f"{temp} {words[idx]}".strip() if temp else words[idx]
                        if len(next_temp.encode("utf-8")) > max_bytes:
                            break
                        # end if
                        temp = next_temp
                        idx += 1
                    # end while
                    if temp:
                        chunks.append(temp)
                    # end if
                    current = " ".join(words[idx:]).strip()
                    if not current:
                        break
                    # end if
                # end while
            else:
                current = candidate
            # end if
        # end for

        if current:
            chunks.append(current)
        # end if

        return chunks or [strtext.strip()]
    # end _chunkText()

# end class CLLMPDFContext
