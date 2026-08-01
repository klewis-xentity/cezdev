from c3dclasses.csystem.cai.cllm.cllm import CLLM
from c3dclasses.ccore.cutility.cutility import readTextFromPDFFilename


class CResearch:
    def __init__(self, id):
        self.m_id = id
        self.m_methods = None
        self.m_images = []
        self.m_cllm = CLLM()
        self.m_cllm.useOllama("llama3.1")
        self.m_cllm.enableMemory(
            "You are a helpful assistant. Remember details the user shares "
            "and use them to answer later questions in the conversation."
        )
        print("Context window:", self.m_cllm.getModelContextWindow())

    def setMethod(self, strmethodid, strmethod):
        self.m_method[strmethodid] = strmethod

    def getRemainingContextSize(self):
        return self.m_cllm.getRemainingContextSize()

    def addImage(self, strimagepath):
        if isinstance(strimagepath, str):
            self.m_images.append(strimagepath)
        elif isinstance(strimagepath, (list, tuple)):
            self.m_images.extend(strimagepath)
        else:
            raise TypeError("strimagepath must be a path string or a list of path strings.")
        return self.m_images

    def prompt(self, strprompt):
        self.m_cllm.setMaxTokens(1000)
        self.m_cllm.prompt(strprompt, self.m_images if self.m_images else None)
        strresponse = self.m_cllm.getPromptResponse()
        return strresponse

    def addArticle(self, strpdfpath):
        strtext = "Am I Wrong, or Is the Autograder Wrong? Effects of AI Grading Mistakes on Learning. Tiffany Wenting Li, Silas Hsu, Max Fowler, Zhilin Zhang, Craig Zilles, and Karrie Karahalios. Department of Computer Science, University of Illinois at Urbana-Champaign, United States; Department of Computer Science, University of Oxford, United Kingdom. ICER '23 V1, August 7–11, 2023, Chicago, IL, USA. Proceedings of the 2023 ACM Conference on International Computing Education Research V.1. ACM. DOI: https://doi.org/10.1145/3568813.3600124. Abstract: Errors in AI grading and feedback are difficult to completely avoid and may negatively affect student learning. This study investigated how incorrect AI grading impacts learning using surveys and interviews with students interacting with an AI autograder for Explain in Plain English (EiPE) code-reading problems. The authors analyzed the effects of false positives (incorrect answers marked correct) and false negatives (correct answers marked incorrect) using causal modeling. False positives were found to significantly harm learning because students often failed to notice the grading errors, paid less attention to feedback after being marked correct, and became biased toward believing their answers were correct. False negatives harmed learning primarily among survey participants, while interview participants were less affected due to deeper behavioral and cognitive engagement with the feedback. The authors propose interface and workflow improvements to help learners detect false positives and encourage deeper reflection on false negatives, reducing the educational harms caused by AI grading mistakes. Keywords: human-AI interaction, AI error, formative feedback, autograder, computer science education, automated short answer grading, Explain in Plain English (EiPE), Bayesian modeling."
        ##readTextFromPDFFilename(strpdfpath)
        return self.prompt(strtext)
    
    
