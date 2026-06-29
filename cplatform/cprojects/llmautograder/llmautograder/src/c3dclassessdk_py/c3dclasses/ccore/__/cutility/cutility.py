#--------------------------------------------------------------------
# name: cutility.py
# desc: contains functions used throughout the generation framework
#--------------------------------------------------------------------
import re
import subprocess
import glob
import os
import logging
import json
from PyPDF2 import PdfReader

#--------------------------------------------------------
# initializes a global logger for the application
#--------------------------------------------------------

# constants
BIN_PATH = "./bin"

# Setup logger
logger = logging.getLogger('CUtility')
logger.setLevel(logging.DEBUG)  # Set logging level (adjust as needed)

# Create file handler
fh = logging.FileHandler('cutility.log')
fh.setLevel(logging.DEBUG)

# Create console handler for debug level
ch = logging.StreamHandler()
ch.setLevel(logging.DEBUG)

# Create formatter and add it to the handlers
formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
fh.setFormatter(formatter)
ch.setFormatter(formatter)

# Add handlers to logger
logger.addHandler(fh)
logger.addHandler(ch)

#---------------------------------------------------
# name: helper functions
#---------------------------------------------------       

#--------------------------------------------------------------------
# name: listFilesInDirectory()
# desc: list all the file of a given regex in a directory
#--------------------------------------------------------------------
def listFilesInDirectory(directory, strregex):
    files = glob.glob(os.path.join(directory, strregex))
    #logger.debug(f"Files matching {strregex} in {directory}: {files}")
    return files
# end listFilesInDirectory()

#----------------------------------------------------
# name: is_pdf_file()
# desc: returns true if the filepath is a pdf file
#---------------------------------------------------- 
def is_pdf_file(file_path):
    result = file_path.lower().endswith('.pdf')
    #logger.debug(f"is_pdf_file({file_path}) -> {result}")
    return result
# is_pdf_file()

#--------------------------------------------------------------------
# name: writeTextFromFilename()
# desc: write text to a file
#--------------------------------------------------------------------
def writeTextToFilename(strfilename, strtext, strmode='w'):
    try:
        with open(strfilename, strmode) as outfile:
            outfile.write(strtext)
        #logger.info(f"Text successfully written to {strfilename}")
    except Exception as e:
        logger.error(f"Error writing to {strfilename}: {e}")
    # end with()
# end writeTextToFilename()

#--------------------------------------------------------------------
# name: readTextFromFilename()
# desc: reads text from a file
#--------------------------------------------------------------------
def readTextFromFilename(strfilename):
    try:
        with open(strfilename, "r") as infile:
            data = infile.read()
        #logger.info(f"Text successfully read from {strfilename}")
        return data
    except Exception as e:
        logger.error(f"Error reading from {strfilename}: {e}")
        return None
    # end with()
# end readTextFromFilename()

#--------------------------------------------------------------------
# name: readTextFromPDFFilename()
# desc: reads text from a pdffile
#--------------------------------------------------------------------
def readTextFromPDFFilename(strpdffilename):
    strtext = ""
    try:
        with open(strpdffilename, "rb") as infile:   
            pdf_reader = PdfReader(infile)
            for page in pdf_reader.pages:
                strtext += page.extract_text()
        logger.info(f"Text extracted from PDF: {strpdffilename}")
    except Exception as e:
        logger.error(f"Error reading PDF {strpdffilename}: {e}")
    return strtext
# end readTextFromPDFFilename()

#--------------------------------------------------------------------
# name: readJSONFromFilename()
# desc: reads json from a filename
#--------------------------------------------------------------------
def readJSONFromFilename(strjsonfilename):
    # Open the JSON file for reading
    with open(strjsonfilename, 'r') as file:
        # Load the contents of the file into a Python dictionary
        return json.load(file)
    return None
# end readJSONFromFilename()

#--------------------------------------------------------------------
# name: writeJSONToFilename()
# desc: writes json to a filename
#--------------------------------------------------------------------
def writeJSONToFilename(strjsonfilename, data):    
    # Write the dictionary to a JSON file
    with open(strjsonfilename, 'w') as file:
        json.dump(data, file, indent=4)  # indent=4 is for p
# end writeJSONToFilename()

#---------------------------------------------------------------------------------------------------
# name: readTextFromFilenameEx()
# desc: reads text from a file containing placeholder variable and filename that point to content
#----------------------------------------------------------------------------------------------------
def readTextFromFilenameEx(strfilename, varlist=None):
    #logger.debug(f"Reading and processing file: {strfilename}")
    content = replaceFilenameReferencesWithContents(readTextFromFilename(strfilename), varlist)
    #logger.debug(f"Processed content from {strfilename}: {content[:100]}...")  # Log first 100 characters
    return content
# end readTextFromFilenameEx()

#---------------------------------------------------------------------------------------------------
# name: extractTextFromFilename()
# desc: returns a code snippet embedded in text
#----------------------------------------------------------------------------------------------------
def extractTextFromFilename(strfilename):
    if(is_pdf_file(strfilename)):
        #logger.debug(f"Extracting text from PDF: {strfilename}")
        return readTextFromPDFFilename(strfilename)
    #logger.debug(f"Extracting text from regular file: {strfilename}")
    return readTextFromFilenameEx(strfilename)
# end extractTextFromFilename()

#--------------------------------------------------------------------
# name: getFilenameReferencesFromText()
# desc: returns filename references (placeholders) found in text
#--------------------------------------------------------------------
def getFilenameReferencesFromText(strText):
    pattern = r'<filename:(.*?)>'
    matches = re.findall(pattern, strText)
    #logger.debug(f"Filename references extracted from text: {matches}")
    return matches
# end getFilenameReferencesFromText()

#--------------------------------------------------------------------
# name: replaceFilenameReferencesWithContents()
# desc: replace placeholder varibles with content from a filename
#--------------------------------------------------------------------
def replaceFilenameReferencesWithContents(strText, varlist=None):
    #logger.debug(f"Replacing filename references in text.")
    strfilenames = getFilenameReferencesFromText(strText)
    for strfilename in strfilenames:
        strFileContents = extractTextFromFilename(strfilename)
        #logger.debug(f"Replacing <filename:{strfilename}> with its contents.")
        strText = strText.replace(f"<filename:{strfilename}>", strFileContents)
        # replace the variables
        if varlist:
            #logger.debug(f"Replacing variable references in text.")
            for varname in varlist:
                varvalue = varlist[varname]
                #logger.debug(f"Replacing <varname:{varname}> with {varvalue}")
                strText = strText.replace(f"<varname:{varname}>", f"{varvalue}")
            # end for
        # end if
    # end for    
    #logger.debug(f"Final replaced text: {strText[:100]}...")  # Log first 100 characters of final text
    return strText
# end replaceFilenameReferencesWithContents()

#---------------------------------------------------------------------------------------------------
# name: extractFilenameFromText()
# desc: same as readTextFromFilenameEx()
#----------------------------------------------------------------------------------------------------
def extractFilenameFromText(strText):
    repattern = r'\b\w+\.py\b'
    match = re.search(repattern, strText)
    result = match.group() if match else None
    #logger.debug(f"extractFilenameFromText({strText[:100]}...) -> {result}")  # Log first 100 characters of input
    return result
# end extractFilenameFromText()

#---------------------------------------------------------------------------------------------------
# name: extractCodeSnippetFromText()
# desc: returns a code snippet embedded in text
#----------------------------------------------------------------------------------------------------
def extractCodeSnippetFromText(strText):
    code_pattern = r'```(.*?)```'  # Regex pattern to match code blocks
    code_matches = re.findall(code_pattern, strText, re.DOTALL)
    #logger.debug(f"Code snippets extracted: {code_matches}")
    return [code_block.strip() for code_block in code_matches]
# end extractCodeSnippetFromText()

#---------------------------------------------------------------------------------------------------
# name: executeCodeFromFilename()
# desc: executes code stored in a filename
#----------------------------------------------------------------------------------------------------
def executeCodeFromFilename(strfilename):
    try:
        result = subprocess.run(['python', strfilename], capture_output=True, text=True)
        logger.info(f"Executed {strfilename}: Return code {result.returncode}")
        #logger.debug(f"Args: {result.args}, Stdout: {result.stdout[:100]}..., Stderr: {result.stderr[:100]}...")  # Log first 100 characters
        return result
    except Exception as e:
        logger.error(f"Error executing {strfilename}: {e}")
        return None
# end executeCodeFromFilename()

#-------------------------------------------------------------------------------------------------------
# name: split_text()
# desc: splites a large body of text due to llm limitation
#-------------------------------------------------------------------------------------------------------
def split_text(text, max_chars=8000):
    if(text == ""):
        return "" 
    chunks = []
    while len(text) > max_chars:
        split_index = text[:max_chars].rfind(". ") + 1  # Split at the nearest sentence end
        if split_index == 0:  # Fallback if no sentence boundary found
            split_index = max_chars
        chunks.append(text[:split_index].strip())
        text = text[split_index:].strip()
    chunks.append(text)
    return chunks
# end split_text()


#----------------------------------------------------------------------
# name: is_compilable()
# desc: determines if the javafiles in a given path are compilable
#----------------------------------------------------------------------
def is_compilable(directory):
    try:
        # Compile all Java files in the specified directory
        result = subprocess.run(
            ["javac", "*.java"],
            cwd=directory,
            shell=True,
            stderr=subprocess.PIPE,
            stdout=subprocess.PIPE
        )

        # Check if there was an error in compiling
        if result.returncode != 0:
            print("Compilation failed with error:")
            print(result.stderr.decode())
            return False

        # the file are compilable
        print("Compilation was successfull!")
        return True

    except Exception as e:
        print(f"An unexpected error occurred: {e}")
        return False
# end is_compilable()


def split_text_into_chunks(text, chunk_size, splitbychar="."):
    """
    Splits a given text into manageable chunks based on the specified chunk size and split character.
    
    Args:
        text (str): The text to split.
        chunk_size (int): The maximum size of each chunk.
        splitbychar (str): The character to prioritize for splitting. Default is '}'.
    
    Returns:
        list: A list of text chunks.
    """
    chunks = []
    while len(text) > chunk_size:
        # Find the best split point based on the split character within the chunk size
        split_point = text[:chunk_size].rfind(splitbychar)
        
        if split_point == -1:  # If the split character is not found, split at the last sentence
            split_point = text[:chunk_size].rfind(". ")
            if split_point == -1:  # If no sentence ending found, force split at chunk size
                split_point = chunk_size
        
        chunks.append(text[:split_point + 1].strip())
        text = text[split_point + 1:].strip()
    
    if text:  # Add any remaining text
        chunks.append(text)
    
    return chunks
# end split_text_into_chunks()

def print_block(strprefix, strsuffix, datatoprint, strlinechar="-"):
    print(f"{strprefix}-begin-{strsuffix}{strlinechar*50}")
    print(f"{datatoprint}")
    print(f"{strprefix}-end-{strsuffix}{strlinechar*50}")
# end print_block()