#---------------------------------------------------
# file: cllmagent.py
# desc: represents an agent configuration
#---------------------------------------------------
from langchain.agents import initialize_agent, AgentType, AgentExecutor
from langchain.tools import Tool
from langchain_community.llms.ollama import Ollama  # Import Ollama from langchain_community
from langchain.memory import ConversationBufferMemory
from pydantic import BaseModel, Field
from cllm.cllm import CLLM

#----------------------------------------------------------
# name: custom_parsing_error_handler()
# desc: parsing error handling
#----------------------------------------------------------
def custom_parsing_error_handler(error):
    # Log the error or apply custom handling here
    print("Parsing error occurred:", error)
    # Return a response or an alternative action
    return "ERROR_HANDLED"  # Placeholder response to prevent retry loop
# end custom_parsing_error_handler()

#----------------------------------------------------
# name: CLLMAgentSettings
# desc: sets up the llm agent setting
#----------------------------------------------------
class CLLMAgentSettings:
     def __init__(self,
                  tools=[],
                  llm="",
                  agent=AgentType.CONVERSATIONAL_REACT_DESCRIPTION,
                  verbose=True,
                  memory=None,
                  output_parser=None,
                  max_steps=1,
                  return_intermediate_steps=False,
                  handle_parsing_errors=custom_parsing_error_handler):
        self.tools = tools
        self.llm = llm
        self.agent = agent
        self.verbose = verbose
        self.memory = memory
        self.output_parser = output_parser
        self.max_steps=max_steps
        self.return_intermediate_steps = return_intermediate_steps
        self.handle_parsing_errors = handle_parsing_errors
    # end __init__()
# end CLLMAgentSettings

#---------------------------------------------------------------
# name: CLLMAgent
# desc: defines and llm agent object
#---------------------------------------------------------------
class CLLMAgent:
    def __init__(self):
        cllm = CLLM()
        self.m_settings = CLLMAgentSettings()
        self.m_settings.llm = cllm.settings().llm
        self.m_settings.memory = ConversationBufferMemory(memory_key="chat_history")
        self.m_agent = None    
    # end __init__():
    
    def settings(self):
        return self.m_settings
    # end settings() 
    
    def create(self, tools):
        self.m_agent = initialize_agent(           
            tools=tools,
            llm=self.m_settings.llm,
            agent=self.m_settings.agent,
            verbose=self.m_settings.verbose,
            handle_parsing_errors=self.m_settings.handle_parsing_errors,
            memory=self.m_settings.memory,
            output_parser=self.m_settings.output_parser,
            max_steps=self.m_settings.max_steps
        )
    # end create()
    
    def prompt(self, strprompt):
        return self.m_agent.run({"input": strprompt})
    # end prompt()
# end CLLMAgent