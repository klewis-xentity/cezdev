#--------------------------------------------------------------
# name: cresponseformat.py
# desc: creates an object to format the response of an llm
#--------------------------------------------------------------
import re
from langchain.output_parsers import PydanticOutputParser

#---------------------------------------------------------------
# name: CResponseFormat
# desc: creates an object to format the response of an llm
#---------------------------------------------------------------
class CResponseFormat:
    def parse_all_code(strtext, strtype):
        code_pattern = r'```(.*?)```'  # Regex pattern to match code blocks
        code_matches = re.findall(code_pattern, strtext, re.DOTALL)
        return [code_block.replace(strtype, "", 1).strip() for code_block in code_matches if strtype in code_block]
    # end parse_all_code()
    
    def parse_code(strtype, strtext):
        allcode = CResponseFormat.parse_all_code(strtext)
        if(allcode and len(allcode) > 0):
            for code in allcode: 
                if(strtype in code):
                    return code.replace(strtype, "").strip()
                # end if
            # end for
            return code.strip()
        # end if
        return "" 
    # end parse_code()
   
    def __init__(self, clsresponse): 
        # create the response object to format the response
        self.m_pydantic_parser = PydanticOutputParser(pydantic_object=clsresponse)
    # end __init__()
    
    def format(self):
        return self.m_pydantic_parser.get_format_instructions()
    # end format()
    
    def parse(self, stresponse):
        return self.m_pydantic_parser.parse(stresponse)
    # end parse()
# end CResponseFormat