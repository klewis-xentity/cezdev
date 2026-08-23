#----------------------------------------------------------------------------------------------------
# file: grade_submission.py
# desc: designed to utilize an llm to grade a students submission
# usage: python .\src\grade_submission.py grade_submission C:/Users/klewi/Desktop/cautograder/data/assignment_3/submissions/0 C:/Users/klewi/Desktop/cautograder/data/rubric_questions.txt C:/Users/klewi/Desktop/cautograder/data/assignment_3/template_code
#----------------------------------------------------------------------------------------------------
import warnings
warnings.filterwarnings("ignore", category=DeprecationWarning)
warnings.filterwarnings("ignore", message=".*LangChain.*")

import os
import json
import sys
from urllib.parse import urljoin
from c3dclasses.csystem.cai.cllm.cllm import CLLM
from c3dclasses.ccore.cutility.cutility import readJSONFromFilename, split_text_into_chunks, writeJSONToFilename, writeTextToFilename, readTextFromFilename, is_compilable
from difflib import SequenceMatcher

try:
    import fire
except ImportError:
    fire = None

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

#--------------------------------------------------------------
# name: generate_feedback_prompt()
# desc: generates a prompt for machine feedback
#--------------------------------------------------------------
def generate_feedback_prompt(questions, source_code, answers):
    prompt = {
        "instructions": (
            "Write concise grading feedback for this Java source code. "
            "Use the rubric questions and the model's rubric answers as evidence. "
            "Summarize the main strengths and the most important improvements. "
            "Return plain text only, with no JSON, markdown, or score table. "
            "Keep the feedback to 2-4 sentences."
        ),
        "questions": questions.strip().split('\n'),
        "rubric_answers": answers,
        "source_code": source_code
    }
    return json.dumps(prompt, indent=4)
# end generate_feedback_prompt()

def get_cloudflare_reauth_url(cllm=None):
    strurl = os.environ.get("CLOUDFLARE_REAUTH_URL", "").strip()
    if strurl:
        return strurl
    # end if

    api_base = getattr(cllm, "api_base", "").strip() if cllm else ""
    if api_base and "localhost" not in api_base and "127.0.0.1" not in api_base:
        return urljoin(api_base.rstrip("/") + "/", "cdn-cgi/access/login")
    # end if
    return ""
# end get_cloudflare_reauth_url()

def should_show_cloudflare_reauth_link(error_msg):
    error_msg = str(error_msg).lower()
    return (
        "agent could not process this request in time" in error_msg or
        "cloudflare" in error_msg or
        "authtoken" in error_msg or
        "auth token" in error_msg or
        "authentication token" in error_msg
    )
# end should_show_cloudflare_reauth_link()

#----------------------------------------------------------------------------------------------------
# class: CSubmissionGrader
# desc: an object designed to grade a submission
#----------------------------------------------------------------------------------------------------
class CSubmissionGrader: 
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
        self.m_strprogramname = os.path.basename(__file__).replace(".py", "")
    # end __init__()
    
    def create(self, strsubmissionpath, strspecfilename, strtemplatepath=""):  
        try:     
            # make some directories to store the rubic input artifacts in
            strhomepath = os.path.dirname(strspecfilename) 
            if not os.path.exists(strhomepath):
                return False
            # end if
            
            # make sure the homepath exist
            strhomepath = f"{strhomepath}/{self.m_strprogramname}"  
            strsubmissionname = os.path.basename(strsubmissionpath)  # Get the filename with the extension
            strdirname = os.path.splitext(strsubmissionname)[0]  # Remove the extension
            strsubmissioncachepath = f"{strhomepath}"
            os.makedirs(f"{strhomepath}", exist_ok=True)
            #os.makedirs(f"{strhomepath}/{strdirname}", exist_ok=True) # this is where all the rubic artifacts will be stored
            self.m_strsubmissionpath = strsubmissionpath
            self.m_strtemplatepath = strtemplatepath
            self.m_strjsonsubmissioncachepath = strsubmissioncachepath
            self.m_strjsonsubmissionfilename = f"{self.m_strjsonsubmissioncachepath}/{strdirname}.json" 
            self.m_strspecfilename = strspecfilename    
            self.m_cllm = CLLM()
            self.m_cllm.useOllama("qwen2.5-coder:7b")
            self.m_strspec = readTextFromFilename(self.m_strspecfilename)
            # Try to load existing submission data (may not exist on first run)
            if os.path.exists(self.m_strjsonsubmissionfilename):
                self.m_jsonsubmission = readJSONFromFilename(self.m_strjsonsubmissionfilename)
            else:
                print(f"  Note: No existing grading cache found, will create new one")
                self.m_jsonsubmission = None
            self.m_strdebug = ""
            print(f"  Submission: {os.path.basename(strsubmissionpath)}")
            print(f"  Rubric:     {os.path.basename(strspecfilename)}")
            print(f"  Template:   {os.path.basename(strtemplatepath)}")
            return True
        # end try
        except Exception as e:
            print(f"[ERROR] Failed to initialize grader: {e}")
            return False
        # end except 
    # end create()
    
    def grade(self):     
        print(f"")
        print(f"Starting grading process...")
        print(f"  LLM model: {self.getLLMModelDescription()}")
        
        # Test LLM connectivity first
        print(f"  Testing LLM connection...")
        try:
            self.m_cllm.setMaxTokens(10)
            self.m_cllm.prompt("Say 'ok'")
            response = self.m_cllm.getPromptResponse()
            if response is None or len(response.strip()) == 0:
                print(f"")
                print(f"  [ERROR] LLM could not be reached - no response received")
                print(f"")
                print(f"  Please ensure:")
                print(f"    1. Ollama is installed and running")
                print(f"    2. Run 'ollama serve' to start the Ollama server")
                print(f"    3. The LLM model is available (e.g., 'ollama pull llama2')")
                return False
            print(f"  LLM connection: OK")
            print(f"")
        except Exception as e:
            error_msg = str(e)
            print(f"")
            print(f"  [ERROR] LLM could not be reached")
            print(f"")
            if "10061" in error_msg or "Connection refused" in error_msg or "NewConnectionError" in error_msg:
                print(f"  The Ollama server is not running.")
                print(f"")
                print(f"  To fix this:")
                print(f"    1. Open a new terminal")
                print(f"    2. Run: ollama serve")
                print(f"    3. Try grading again")
            else:
                print(f"  Details: {error_msg[:100]}..." if len(error_msg) > 100 else f"  Details: {error_msg}")
            self.printCloudflareReauthMessageIfNeeded(error_msg)
            print(f"")
            return False
        
        return self.gradeSubmissionSummary() 
    # end grade()

    def getLLMModelDescription(self):
        strplatform = getattr(self.m_cllm, "model_platform", "Unknown")
        strmodel = getattr(self.m_cllm, "model", "Unknown")
        return f"{strmodel} ({strplatform})"
    # end getLLMModelDescription()

    def printCloudflareReauthMessageIfNeeded(self, error_msg):
        if not should_show_cloudflare_reauth_link(error_msg):
            return
        # end if

        strurl = get_cloudflare_reauth_url(self.m_cllm)
        print(f"")
        print(f"  Cloudflare authentication may need to be refreshed.")
        if strurl:
            print(f"  Reauthenticate Cloudflare Auth token: {strurl}")
        else:
            print(f"  Set CLOUDFLARE_REAUTH_URL to the Cloudflare Access login URL for this LLM service.")
        # end if
    # end printCloudflareReauthMessageIfNeeded()
    
    def gradeMetaData(self, similarity_percent_per_file):   
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
    # end grade_similarity_percentages()
    
    def gradeSubmissionSummary(self):
        if(self.m_jsonsubmission == None):
            self.m_jsonsubmission = {}
        # end if
        
        # check if the code is compilable, or similar to template
        self.m_bcompliable = is_compilable(self.m_strsubmissionpath)
        self.m_similarity_percentage = self.computeSubmissionTemplateSimilarityPercentage(0.90)
        self.m_bcomplete = not (self.m_similarity_percentage > 0.85)
        print(f"  Compilable: {'Yes' if self.m_bcompliable else 'No'}")
        print(f"  Complete:   {'Yes' if self.m_bcomplete else 'No'}")
        
        # get all the files
        strfiles = [f for f in os.listdir(self.m_strsubmissionpath)] 
        strfiles = [strfile for strfile in strfiles if strfile.endswith(".java")]
        i = 0
        count = len(strfiles)
        print(f"  Files found: {count}")
        print("")
        for strfile in strfiles:
            strfilepath = f"{self.m_strsubmissionpath}/{strfile}"
            try:
                if(strfile.endswith(".java")):
                    strcode = readTextFromFilename(strfilepath)
                    strquestions = self.m_strspec
                    print(f"  Processing: {strfile} ({len(strcode)} chars)")
                    self.gradeSubmissionSummaryOfAFile(strfile, strcode, strquestions) 
                # end if     
                i += 1
            # end try
            except Exception as e:
                error_msg = str(e)
                print(f"    [ERROR] Could not process {strfile}: {error_msg[:100]}..." if len(error_msg) > 100 else f"    [ERROR] Could not process {strfile}: {error_msg}")
                self.printCloudflareReauthMessageIfNeeded(error_msg)
                i += 1
                continue
            # end except
        # end for
        
        writeJSONToFilename(self.m_strjsonsubmissionfilename, self.m_jsonsubmission)
        writeTextToFilename("./debugtext.txt", self.m_strdebug)
        return True
    # end gradeSubmissionSummary()

    def submissionFileAlreadyProcessed(self, strfilename):
        if(self.m_jsonsubmission and 
           "filenames" in self.m_jsonsubmission and
           strfilename in self.m_jsonsubmission["filenames"] and 
           self.m_jsonsubmission["filenames"][strfilename] is not None and
           "machine_feedback" in self.m_jsonsubmission and
           strfilename in self.m_jsonsubmission["machine_feedback"] and
           self.m_jsonsubmission["machine_feedback"][strfilename] is not None):
            print(f"    -> Skipping (already processed): {strfilename}")
            return True
        # end if
        return False
    # end submissionFileAlreadyProcessed()
    
    def gradeSubmissionSummaryOfAFile(self, strfilename, strfilecode, strquestiontoask):
        codefilesize = len(strfilecode)
        codefilesizelimit = 4500
        strquestions = strquestiontoask.strip().split('\n')
        nquestions = len(strquestions)
        
        if(codefilesize > codefilesizelimit):
            print(f"    -> Splitting large file into chunks...")
            strtextchunks = split_text_into_chunks(strfilecode, codefilesizelimit, "}")
            for index, strtextchunk in enumerate(strtextchunks):
                print(f"    -> Processing chunk {index + 1}/{len(strtextchunks)}")
                self.gradeSubmissionSummaryOfAFile(f"{strfilename}.{index}", strtextchunk, strquestiontoask)
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
        
        try:
            strprompt = generate_grading_prompt(strquestiontoask, strfilecode)
        except Exception as e:
            print(f"    [ERROR] Could not generate prompt: {e}")
        
        print(f"    -> Sending to LLM...")
        self.m_cllm.setMaxTokens(1000)
        self.m_cllm.prompt(strprompt)
        strresponse = self.m_cllm.getPromptResponse()
                     
        jsondata = self.m_cllm.parseJSON()
        strresponse = ""
        while (jsondata == None or len(jsondata) != nquestions) and reprompt_attempt > 0: 
            print(f"    -> Retrying... ({reprompt_attempt} attempts remaining)")
            self.m_cllm.setMaxTokens(1000)
            strnewprompt = f"\nMake sure the output is in JSON array of {nquestions} length. Try to use this information here along with the prompt\n\n{strprompt}\n\nInformation:\n\n{strresponse}."    
            self.m_cllm.prompt(strnewprompt)
            strresponse = self.m_cllm.getPromptResponse()
            jsondata = self.m_cllm.parseJSON()
            reprompt_attempt -= 1
        # end while
    
        jsonfilesummary = []
        strfeedback = ""
        self.m_strdebug += f"{strfilename}\n"
        i=0
        for answer in jsondata:
            jsonfilesummary.append(self.compute_score(answer, strtoken="-")) 
            strquestion = strquestions[i] 
            self.m_strdebug += f"{answer} - {strquestion}\n"
            i += 1
        # end for
        self.m_strdebug += f"\n\n"

        try:
            print(f"    -> Generating machine feedback...")
            self.m_cllm.setMaxTokens(350)
            self.m_cllm.prompt(generate_feedback_prompt(strquestiontoask, strfilecode, jsondata))
            strfeedback = self.m_cllm.getPromptResponse().strip()
        except Exception as e:
            print(f"    [WARNING] Could not generate machine feedback: {e}")
            strfeedback = ""
        # end try
              
        if(self.m_jsonsubmission is None):
            self.m_jsonsubmission = {}
        # end if
        if("filenames" not in self.m_jsonsubmission):
            self.m_jsonsubmission["filenames"] = {}
        # end if
        if("machine_feedback" not in self.m_jsonsubmission):
            self.m_jsonsubmission["machine_feedback"] = {}
        # end if
        if("metadata" not in self.m_jsonsubmission):
            self.m_jsonsubmission["metadata"] = {}
        # end if
        
        # store the submission scores
        self.m_jsonsubmission["filenames"][strfilename] = jsonfilesummary if (jsondata) else None        
        self.m_jsonsubmission["machine_feedback"][strfilename] = strfeedback if (jsondata) else None
        self.m_jsonsubmission["metadata"] = jsonmetadatasummary if (jsondata) else None        
        writeJSONToFilename(self.m_strjsonsubmissionfilename, self.m_jsonsubmission)
        print(f"    -> Done")
        return True
    # end gradeSubmissionSummaryOfAFile()

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
        score = (existance * quantity * weight) + (existance * quality * weight)
        return score        
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
        bisSubmissionTheSameAsTemplate = (numsame / numtemplatefiles) >= num_of_files_percentage
        return bisSubmissionTheSameAsTemplate
    # end isSubmissionTheSameAsTemplate()
# end CSubmissionGrader

#----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# name: grade_submission()
# desc: grade summaries for a submission file
# usage: python.exe .\CSubmissionGrader\CSubmissionGrader.py grade submissionspath specification.txt templatecodepath
#----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
def grade_submission(strsubmissionpath, strspecfilename, strtemplatecodepath):
    print(f"  Submission path: {strsubmissionpath}")
    print(f"  Rubric path:     {strspecfilename}")
    print(f"  Template path:   {strtemplatecodepath}")
    print(f"")
    css = CSubmissionGrader()
    if css.create(strsubmissionpath, strspecfilename, strtemplatecodepath):
        success = css.grade()
        if success:
            print(f"")
            print(f"[OK] Grading completed successfully")
        # Error already displayed in grade()
    else:
        print(f"")
        print(f"  [ERROR] Failed to initialize grader")
        print(f"")
        print(f"  Check that these paths exist:")
        print(f"    - Submission: {strsubmissionpath}")
        print(f"    - Rubric:     {strspecfilename}")
        print(f"    - Template:   {strtemplatecodepath}")
        print(f"")
# end grade_submission()

#----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# name: grade_submission_meta_data()
# desc: grade summaries for a submission file
# usage: python.exe .\CSubmissionGrader\CSubmissionGrader.py grade submissionspath specification.txt templatecodepath
#----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
def grade_submission_meta_data(strsubmissionpath, strspecfilename, strtemplatecodepath):
    print(f"  Submission path: {strsubmissionpath}")
    print(f"  Rubric path:     {strspecfilename}")
    print(f"  Template path:   {strtemplatecodepath}")
    print(f"")
    css = CSubmissionGrader()
    if css.create(strsubmissionpath, strspecfilename, strtemplatecodepath):
        css.gradeMetaData(0.90)
        print(f"")
        print(f"[OK] Meta data grading completed successfully")
    else:
        print(f"")
        print(f"  [ERROR] Failed to initialize grader")
        print(f"")
        print(f"  Check that these paths exist:")
        print(f"    - Submission: {strsubmissionpath}")
        print(f"    - Rubric:     {strspecfilename}")
        print(f"    - Template:   {strtemplatecodepath}")
        print(f"")
# end grade_submission_meta_data()

#----------------------------------------
# main entry point
#----------------------------------------
if __name__ == "__main__":
    if fire is not None:
        fire.Fire()
    else:
        commands = {
            "grade_submission": grade_submission,
            "grade_submission_meta_data": grade_submission_meta_data,
        }
        if len(sys.argv) < 2 or sys.argv[1] not in commands:
            print(
                "Usage: grade_submission.py "
                "<grade_submission|grade_submission_meta_data> "
                "<submission_path> <rubric_path> <template_code_path>"
            )
            sys.exit(1)
        commands[sys.argv[1]](*sys.argv[2:])
# end if
