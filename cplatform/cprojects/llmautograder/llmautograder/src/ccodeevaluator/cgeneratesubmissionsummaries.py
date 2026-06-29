#----------------------------------------------------------------------------------------------------
# file: cgeneratesubmissionsummaries.py
# desc: generates summary for a submission
#----------------------------------------------------------------------------------------------------
import subprocess
import fire
import os
import json
from cllm.cllm import CLLM
from cutility.cutility import extractTextFromFilename, readJSONFromFilename, split_text_into_chunks, writeJSONToFilename, writeTextToFilename, readTextFromFilename, is_compilable
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain.chains.summarize import load_summarize_chain
from difflib import SequenceMatcher

#----------------------------------------------------------------------------------------------------
# class: CGenerateSubmissionSummaries
# desc: generates summary for a submission
#----------------------------------------------------------------------------------------------------
class CGenerateSubmissionSummaries: 
    def __init__(self):  
        self.m_cllm = None      
        self.m_strspecfilename = ""
        self.m_strspec = ""
        self.m_strtemplatepath = ""
        self.m_strsubmissionpath = ""
        self.m_strjsonsubmissionfilename = ""
        self.m_jsonsubmission = None
        self.m_bcompliable = True
        self.m_bcomplete = True
        self.m_similarity_percentage = 0.0
        self.m_numoftemplatefiles = 0
        self.m_numofnontemplatefiles = 0
        self.m_numofsubmittedfiles = 0
        self.m_strdebug = ""
    # end __init__()
    
    def create(self, strsubmissionpath, strspecfilename, strtemplatepath=""):  
        try:     
            # make some directories to store the rubic input artifacts in
            strhomepath = os.path.dirname(strspecfilename)
            if not os.path.exists(strhomepath):
                return False
            # end if
            strsubmissionname = os.path.basename(strsubmissionpath)  # Get the filename with the extension
            strdirname = os.path.splitext(strsubmissionname)[0]  # Remove the extension
            strsubmissioncachepath = f"{strhomepath}/.cache/submissions/{strdirname}"
            os.makedirs(f"{strhomepath}/.cache", exist_ok=True)
            os.makedirs(f"{strhomepath}/.cache/submissions", exist_ok=True)
            os.makedirs(f"{strhomepath}/.cache/submissions/{strdirname}", exist_ok=True) # this is where all the rubic artifacts will be stored
            self.m_strsubmissionpath = strsubmissionpath
            self.m_strtemplatepath = strtemplatepath
            self.m_strjsonsubmissioncachepath = strsubmissioncachepath
            self.m_strjsonsubmissionfilename = f"{self.m_strjsonsubmissioncachepath}/submission.json" 
            self.m_strspecfilename = strspecfilename    
            self.m_cllm = CLLM()
            self.m_strspec = readTextFromFilename(self.m_strspecfilename)
            self.m_jsonsubmission = readJSONFromFilename(self.m_strjsonsubmissionfilename)
            self.m_strdebug = ""
            return True
        # end try
        except:
            return False
        # end except 
    # end create()
    
    def generate(self):     
        return self.generateSubmissionSummary() 
    # end generate()
    
    def generate_meta_data(self, similarity_percent_per_file):   
        strsubmissionfiles = {f:f for f in os.listdir(self.m_strsubmissionpath) if f.endswith('.java')} 
        strtemplatefiles = {f:f for f in os.listdir(self.m_strtemplatepath) if f.endswith('.java')}
        numtemplatefiles = len(strtemplatefiles)
        numsame = 0
        numtemplatefiles = 0
        for strfile in strsubmissionfiles:
            if(strfile in strtemplatefiles):
                strcontents1 = readTextFromFilename(f"{self.m_strsubmissionpath}/{strfile}")
                strcontents2 = readTextFromFilename(f"{self.m_strtemplatepath}/{strfile}")
                similarity = SequenceMatcher(None, strcontents1, strcontents2).ratio()
                if(similarity > similarity_percent_per_file): 
                    numsame += 1
                numtemplatefiles += 1 
            # end if
        # end for
        self.m_bcompliable = is_compilable(self.m_strsubmissionpath)
        self.m_numoftemplatefiles = numtemplatefiles
        self.m_numofnontemplatefiles = len(strsubmissionfiles) - numtemplatefiles
        self.m_numofsubmittedfiles = len(strsubmissionfiles)
        self.m_similarity_percentage = (numsame / numtemplatefiles)
        self.m_bcomplete = not (self.m_similarity_percentage > 0.85)
        
        jsonmetadatasummary = {}
        strcompilable = "yes - all - excellent" if(self.m_bcompliable) else "no - none - fail"
        strcomplete = "yes - all - excellent" if(self.m_bcomplete) else "no - none - fail"
        jsonmetadatasummary["compilable"] = self.compute_score(strcompilable)
        jsonmetadatasummary["complete"] = self.compute_score(strcomplete)
        jsonmetadatasummary["similarity_percentage"] = self.m_similarity_percentage
        jsonmetadatasummary["number_template_files"] = self.m_numoftemplatefiles
        jsonmetadatasummary["number_nontemplate_files"] = self.m_numofnontemplatefiles 
        jsonmetadatasummary["number_sumbitted_files"] = self.m_numofsubmittedfiles     
        jsonmetadatasummary["percentage_of_template_files"] =  self.m_numoftemplatefiles / self.m_numofsubmittedfiles     
        jsonmetadatasummary["percentage_of_nontemplate_files"] =  self.m_numofnontemplatefiles / self.m_numofsubmittedfiles     
        if(self.m_jsonsubmission is None):
            self.m_jsonsubmission = {}
        # end if
        if("metadata" not in self.m_jsonsubmission):
            self.m_jsonsubmission["metadata"] = {}
        # end if        
        # store the submission scores
        self.m_jsonsubmission["metadata"] = jsonmetadatasummary   
        writeJSONToFilename(self.m_strjsonsubmissionfilename, self.m_jsonsubmission)              
    # end generate_similarity_percentages()
    
    def generateSubmissionSummary(self):
        if(self.m_jsonsubmission == None):
            self.m_jsonsubmission = {}
        # end if
        self.m_bcompliable = is_compilable(self.m_strsubmissionpath)
        self.m_similarity_percentage = self.computeSubmissionTemplateSimilarityPercentage(0.90)
        self.m_bcomplete = not (self.m_similarity_percentage > 0.85)
        strfiles = [f for f in os.listdir(self.m_strsubmissionpath)] 
        strfiles = [strfile for strfile in strfiles if strfile.endswith(".java")]
        i = 0
        count = len(strfiles)
        for strfile in strfiles:
            strfilepath = f"{self.m_strsubmissionpath}/{strfile}"
            try:
                if(strfile.endswith(".java")):
                    print(f"Processing file ({i+1}/{count}): {strfile} .....")
                    strcode = readTextFromFilename(strfilepath)
                    strquestions = self.m_strspec
                    self.generateSubmissionSummaryOfAFile(strfile, strcode, strquestions)
                # end if     
                i += 1
            # end try
            except:
                i += 1
                continue
            # end except
        # end for
        print(self.m_jsonsubmission)
    
        try: 
            print("Number of filenames: ", len(self.m_jsonsubmission['filenames']))
        except:
            pass
        #
        writeJSONToFilename(self.m_strjsonsubmissionfilename, self.m_jsonsubmission)   
        writeTextToFilename("./debugtext.txt", self.m_strdebug) 
        return True
    # end generateSubmissionSummary()

    def submissionFileAlreadyProcessed(self, strfilename):
        if(self.m_jsonsubmission and 
           strfilename in self.m_jsonsubmission["filenames"] and 
           self.m_jsonsubmission["filenames"][strfilename] is not None):
            print(f"Already processed this file's summary: {strfilename}. Skipping processing.....")
            return True
        print(f"Will process this file's summary: {strfilename}.")
        return False
    # end submissionFileAlreadyProcessed()
    
    def generateSubmissionSummaryOfAFile(self, strfilename, strfilecode, strquestiontoask):
        codefilesize = len(strfilecode)
        codefilesizelimit = 4500
        strquestions = strquestiontoask.strip().split('\n')
        nquestions = len(strquestions)
        if(codefilesize > codefilesizelimit):
            strtextchunks = split_text_into_chunks(strfilecode, codefilesizelimit, "}")
            for index, strtextchunk in enumerate(strtextchunks):            
                self.generateSubmissionSummaryOfAFile(f"{strfilename}.{index}", strtextchunk, strquestiontoask)
            # end for
            return True
        # end if
        # check if the file already exist
        strfilename = strfilename.lower().strip().replace(" ","_")
        if(strfilename == ""):
            return False
        # end if
        if(self.submissionFileAlreadyProcessed(strfilename)):
            return True
        # end if 
        reprompt_attempt = 3
        jsonmetadatasummary = {}
        strcompilable = "yes - all - excellent" if(self.m_bcompliable) else "no - none - fail"
        strcomplete = "yes - all - excellent" if(self.m_bcomplete) else "no - none - fail"
        jsonmetadatasummary["compilable"] = self.compute_score(strcompilable)
        jsonmetadatasummary["complete"] = self.compute_score(strcomplete)
        jsonmetadatasummary["similarity_percentage"] = self.m_similarity_percentage
        jsonmetadatasummary["number_template_files"] = self.m_numoftemplatefiles
        jsonmetadatasummary["number_nontemplate_files"] = self.m_numofnontemplatefiles 
        jsonmetadatasummary["number_sumbitted_files"] = self.m_numofsubmittedfiles     
        jsonmetadatasummary["percentage_of_template_files"] =  self.m_numoftemplatefiles / self.m_numofsubmittedfiles     
        jsonmetadatasummary["percentage_of_nontemplate_files"] =  self.m_numofnontemplatefiles / self.m_numofsubmittedfiles     
        self.m_strdebug += f"compilable: {strcompilable}\n"
        self.m_strdebug += f"complete: {strcomplete}\n"
        
        strprompt = generate_grading_prompt(strquestiontoask, strfilecode)
        self.m_cllm.setMaxTokens(1000)
        self.m_cllm.prompt(strprompt)
        print(self.m_cllm.getPromptResponse())
        jsondata = self.m_cllm.parseJSON()
        strresponse = ""
        while (jsondata == None or len(jsondata) != nquestions) and reprompt_attempt > 0: 
            print(f"Retrying.... - Will retry {reprompt_attempt} times.")
            self.m_cllm.setMaxTokens(1000)
            self.m_cllm.prompt(f"\nMake sure the output is in JSON array of {nquestions} length. Try to use this information here along with the prompt\n\n{strprompt}\n\nInformation:\n\n{strresponse}.")
            strresponse = self.m_cllm.getPromptResponse()
            print(strresponse)
            jsondata = self.m_cllm.parseJSON()
            reprompt_attempt -= 1
        # end while
    
        jsonfilesummary = []
        self.m_strdebug += f"{strfilename}\n"
        i=0
        for answer in jsondata:
            jsonfilesummary.append(self.compute_score(answer, strtoken="-")) 
            strquestion = strquestions[i] 
            self.m_strdebug += f"{answer} - {strquestion}\n"
            i += 1
        # end for
        self.m_strdebug += f"\n\n"
        
        if(self.m_jsonsubmission is None):
            self.m_jsonsubmission = {}
        # end if
        if("filenames" not in self.m_jsonsubmission):
            self.m_jsonsubmission["filenames"] = {}
        # end if
        if("metadata" not in self.m_jsonsubmission):
            self.m_jsonsubmission["metadata"] = {}
        # end if
        
        # store the submission scores
        self.m_jsonsubmission["filenames"][strfilename] = jsonfilesummary if (jsondata) else None        
        self.m_jsonsubmission["metadata"] = jsonmetadatasummary if (jsondata) else None        
        writeJSONToFilename(self.m_strjsonsubmissionfilename, self.m_jsonsubmission)
        return True
    # end generateSubmissionSummaryOfAFile()

    def compute_score(self, strinfo, strtoken="-"):
        word2score = {
            # Existence
            "yes": 1.0,
            "no": 0.0,
            # Quantity
            "all": 1.0,
            "alot": 0.79,
            "some": 0.59,
            "little":0.49,
            "none": 0.39,
            # Quality
            "excellent": 1.0,
            "good": 0.79,
            "satisfactory": 0.59,
            "poor": 0.10,
        }

        # Split the input string and map scores
        factors = [factor.strip() for factor in strinfo.split(strtoken)]        
        if factors[0] not in word2score:
            factors[0] = "yes"
        if factors[1] not in word2score:
            factors[1] = "some"
        if factors[2] not in word2score:
            factors[2] = "satisfactory"
        
        existance = word2score.get(factors[0], 0.0) 
        quantity = word2score.get(factors[1], 0.0)
        quality = word2score.get(factors[2], 0.0)

        # Calculate weighted score
        weight = 0.5  # Quality and quantity have equal importance
        return (existance * quantity * weight) + (existance * quality * weight)        
    # end compute_score()

    def computeSubmissionTemplateSimilarityPercentage(self, similarity_percent_per_file):
        strsubmissionfiles = {f:f for f in os.listdir(self.m_strsubmissionpath) if f.endswith('.java')} 
        strtemplatefiles = {f:f for f in os.listdir(self.m_strtemplatepath) if f.endswith('.java')}
        numtemplatefiles = len(strtemplatefiles)
        numsame = 0
        numtemplatefiles = 0
        for strfile in strsubmissionfiles:
            if(strfile in strtemplatefiles):
                strcontents1 = readTextFromFilename(f"{self.m_strsubmissionpath}/{strfile}")
                strcontents2 = readTextFromFilename(f"{self.m_strtemplatepath}/{strfile}")
                similarity = SequenceMatcher(None, strcontents1, strcontents2).ratio()
                if(similarity > similarity_percent_per_file): 
                    numsame += 1
                numtemplatefiles += 1 
            # end if
        # end for
        
        self.m_numoftemplatefiles = numtemplatefiles
        self.m_numofnontemplatefiles = len(strsubmissionfiles) - numtemplatefiles
        self.m_numofsubmittedfiles = len(strsubmissionfiles)
      
        # percentage of diferent files
        percentage_diff_in_template_files = (numsame / numtemplatefiles)
        return (percentage_diff_in_template_files)   
    # end     
    
    def isSubmissionTheSameAsTemplate(self, similarity_percent_per_file, num_of_files_percentage):
        strsubmissionfiles = {f:f for f in os.listdir(self.m_strsubmissionpath) if f.endswith('.java')} 
        strtemplatefiles = {f:f for f in os.listdir(self.m_strtemplatepath) if f.endswith('.java')}
        numtemplatefiles = len(strtemplatefiles)
        numsame = 0
        for strfile in strsubmissionfiles:
            if(strfile in strtemplatefiles):
                strcontents1 = readTextFromFilename(f"{self.m_strsubmissionpath}/{strfile}")
                strcontents2 = readTextFromFilename(f"{self.m_strtemplatepath}/{strfile}")
                similarity = SequenceMatcher(None, strcontents1, strcontents2).ratio()
                if(similarity > similarity_percent_per_file): 
                    numsame += 1        
            # end if
        # end for
        return (numsame / numtemplatefiles) >= num_of_files_percentage   
    # end isSubmissionTheSameAsTemplate()
# end CGenerateSubmissionSummaries

#----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# name: generate()
# desc: generate summaries for a submission file
# usage: python.exe .\cgeneratesubmissionsummaries\cgeneratesubmissionsummaries.py generate submissionspath specification.txt templatecodepath
#----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
def generate(strsubmissionpath, strspecfilename, strtemplatecodepath):
    css = CGenerateSubmissionSummaries()
    css.create(strsubmissionpath, strspecfilename, strtemplatecodepath)
    css.generate()
    #test(strsubmissionpath, strspecfilename, strtemplatecodepath)
# end generate()

#----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# name: generate_similarity_percentages()
# desc: generate summaries for a submission file
# usage: python.exe .\cgeneratesubmissionsummaries\cgeneratesubmissionsummaries.py generate submissionspath specification.txt templatecodepath
#----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
def generate_meta_data(strsubmissionpath, strspecfilename, strtemplatecodepath):
    css = CGenerateSubmissionSummaries()
    css.create(strsubmissionpath, strspecfilename, strtemplatecodepath)
    css.generate_meta_data(0.90)
    #test(strsubmissionpath, strspecfilename, strtemplatecodepath)
# end generate()

#----------------------------------------
# main entry point
#----------------------------------------
if __name__ == "__main__":
    fire.Fire()
# end if

#--------------------------------------------------------------
# name: generate_grading_prompt()
# desc: generates a grading prompt 
#--------------------------------------------------------------
def generate_grading_prompt(questions, source_code):
    
    """
    Generates a grading prompt that outputs a JSON array of answers for each question.
    
    Args:
        questions (str): A string containing the questions for evaluating the source code separated by newlines.
        source_code (str): The source code to be evaluated.
        
    Returns:
        str: A formatted prompt for grading.
    """
    # Split the questions into a list
    question_list = questions.strip().split('\n')
    numquestions = len(question_list)
    
    strexistance = "[yes/no]"
    strquality = "[excellent/good/satisfactory/poor/fail]"
    strquantity = "[all/alot/many/some/little/none]"
    
    # Construct the grading prompt
    prompt = {
        "instructions": (
            "Make sure just output the answers in JSON format below an nothing else. "
            f"For each of the following {numquestions} questions about the source code, provide your answer in the format: "
            f"{strexistance} - {strquantity} - {strquality}.\n"
            "Your answers should be in the form of a JSON array, with each answer corresponding to a specific question. "
            "For example, the output should look precisely and exactly like this:\n\n"
            "```json\n[\n\"yes - all - excellent\",\n\"yes - some - satifactory\",\n...\n\"yes - some - satifactory\"\n]\n```"
            "Should not look like this:\n\n"
            "```json\n[\n\"yes\" - \"all\" - \"excellent\",\n\"yes\" - \"some\" - \"satifactory\",\n...\n\"yes\" - \"alot\" - \"good\"\n]\n```"
            "Put output here within this ```json inside here ```:\n```json\n<output goes here>\n```\n"
        ),
        "questions": question_list,
        "source_code": source_code
    }
    
    return json.dumps(prompt, indent=4)
# end generate_grading_prompt()