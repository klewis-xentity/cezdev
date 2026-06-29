#-------------------------------------------------------------------------------------
# name: cpromptformat.py
# desc: an object that formats the prompt to be consume effectively by the llm
#-------------------------------------------------------------------------------------

from langchain.prompts import ChatPromptTemplate

#-------------------------------------------------------------------------------------
# name: CPromptFormat
# desc: an object that formats the prompt to be consume effectively by the llm
#-------------------------------------------------------------------------------------
class CPromptFormat:
    def __init__(self, strtemplate): 
        self.m_template = ChatPromptTemplate.from_template(strtemplate)
    # end __init__()
    
    def format(self, **params):
        return self.m_template.format_messages(**params)
    # end format()
# end CPromptFormat

