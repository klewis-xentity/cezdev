#---------------------------------------------------------------------------------------------------------------
# file: cvectordatabase.py
# desc: defines a vector database that stores embeddings and matching documents for LLM to utilize
#---------------------------------------------------------------------------------------------------------------
import os
import shutil
import fire
from PyPDF2 import PdfReader
from langchain_community.document_loaders import PyPDFDirectoryLoader
from langchain_community.document_loaders import TextLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter, CharacterTextSplitter
from langchain.schema.document import Document
from langchain_chroma import Chroma
from langchain_community.vectorstores import FAISS
from langchain_community.embeddings.ollama import OllamaEmbeddings
from langchain_community.embeddings.openai import OpenAIEmbeddings
from langchain_community.embeddings.bedrock import BedrockEmbeddings
from cutility.clogger import CLogger
from cutility.cutility import extractTextFromFilename

# Setup the logger
clogger = CLogger()
clogger.create("CVectorDatabase")
logger = clogger.get()

#-----------------------------------------------------------
# name: CVectorDatabaseSettings
# desc: setting for the vector database
#-----------------------------------------------------------
class CVectorDatabaseSettings:
    def __init__(self,  
                 strvectordbpath="C:/Users/klewi/Desktop/cezdev/cprojects/cautograder/cutility/chroma",
                 strdatadbpath="C:/Users/klewi/Desktop/cezdev/cprojects/cautograder/cutility/data",
                 strmodel="mxbai-embed-large"):
        self.m_strvectordbpath = strvectordbpath
        self.m_strdatadbpath = strdatadbpath 
        self.m_strmodel = strmodel
        self.setOllamaEmbeddingsFunction()
        self.m_vectordb = None
        self.m_embed_documents = None
    # end __init__()
    def setOpenAIEmbeddingsFunction(self):
        self.m_fnembeddings = OpenAIEmbeddings()
        return self
    # end setOpenAIEmbeddingsFunction()
    def setOllamaEmbeddingsFunction(self):
        self.m_fnembeddings = OllamaEmbeddings(model=self.m_strmodel)
        return self
    # end setOllamaEmbeddingsFunction():
    def setBedrockEmbeddingsFunction(self):
        self.m_fnembeddings = BedrockEmbeddings()
        return self
    # end setBedrockEmbeddingsFunction()
    def setVectorDBPath(self, strpath):
        self.m_strvectordbpath = strpath 
    # end setVectorDBPath()
    def setDataDBPath(self, strpath):
        self.m_strdatadbpath = strpath
        return self
    # end setDataDBPath()
# end CVectorDatabaseSettings

#-------------------------------------------------------------------------------------------------------
# name: CSimpleVectorDatabase
# desc: defines a vector database that stores embeddings and matching text documents
#--------------------------------------------------------------------------------------------------------
class CSimpleVectorDatabase:
    def __init__(self):
        self.m_settings = CVectorDatabaseSettings()     
        self.m_db = None   
        self.m_strtext = ""
        self.m_metadata = None
        self.m_lastindex = 0
    # end __init__()

    def createFromText(self):
        return 
    # end createFromText()

    def create(self):
        if(self.m_strtext != ""):
            self.createMetaData() 
        # end if
        
        self.m_db = FAISS.from_texts(
            texts=[chunk["page_content"] for chunk in self.m_metadata],
            embedding=self.m_settings.m_fnembeddings,
            metadatas=self.m_metadata
        )
        
        return self.m_db is not None
    # end createFromText()
    
    def addTextFromFile(self, strfilename):
        self.m_strtext += extractTextFromFilename(strfilename) 
    # end addTextFromFile()
    
    def addText(self, strtext, metadata=None):
        self.m_strtext += strtext 
    # end addTextFromFile()

    def createMetaData(self, chunkSize=2000, inmetadata=None):
        # Split into chunks
        text_splitter = CharacterTextSplitter(
            separator="\n",
            chunk_size=2000,
            chunk_overlap=200,
            length_function=len
        )
        
        # convert the text into chunks
        chunks = text_splitter.split_text(self.m_strtext)
        
        if not chunks:
            raise ValueError("Chunks list is empty. Cannot create FAISS index.")
        # end if
        
        if not self.m_metadata:
            self.m_metadata = []
        # end if
            
        # process each chucnk and label its metadata
        index = self.m_lastindex
        for i, chunk in enumerate(chunks):
            if(inmetadata):
                self.m_metadata.append({"page_content": chunk, "chunk_index": index, **inmetadata} )
            # end if
            else:
                self.m_metadata.append({"page_content": chunk, "chunk_index": index})
            # end else
            index += 1
        # end for
        
        # reset the text  
        self.m_strtext = ""
        self.m_lastindex = index
        return True
    # end createMetaData()  
        
    
    
    def getSettings(self):
        return self.m_settings
    # end getSettings()

    def query(self, strquery):
        try:
            # Search the DB for resulting documents
            return self.m_db.similarity_search(strquery)
        # end try
        except Exception as e:
            logger.error(f"Error during query call: {e} with these settings: {self}")
            return None
        # end except
    # end query()
# end CSimpleVectorDatabase

#-------------------------------------------------------------------------------------------------------
# name: CVectorDatabase
# desc: defines a vector database that stores embeddings and matching documents for LLM to utilize
#--------------------------------------------------------------------------------------------------------
class CVectorDatabase:    
    def __init__(self):
        self.m_settings = CVectorDatabaseSettings()     
        self.m_db = None   
    # end __init__()    

    def getSettings(self):
        return self.m_settings
    # end getSettings()

    def create(self):
        # Prepare the DB.
        self.m_db = Chroma(
            persist_directory=self.m_settings.m_strvectordbpath, 
            embedding_function=OllamaEmbeddings(model="mxbai-embed-large")
            #embedding_function=self.m_settings.m_fnembeddings
        )
        return self.m_db is not None
    # end create()
            
    def loadPDFDocuments(self):
        try:
            document_loader = PyPDFDirectoryLoader(self.m_settings.m_strdatadbpath)
            documents = document_loader.load()
            text_splitter = RecursiveCharacterTextSplitter(
                chunk_size=800,
                chunk_overlap=80,
                length_function=len,
                is_separator_regex=False,
            )
            chunks = text_splitter.split_documents(documents)
            self._addDocument(chunks)
            return True
        # end try  
        except Exception as e:
            logger.error(f"Error during load call: {e} with these settings: {self}")
            return False
        # end except
    # end loadPDFDocuments()
    
    def loadDocumentsFromDataDir(self):
        strpath = self.m_settings.m_strdatadbpath
        strfilenames = [f for f in os.listdir(self.m_settings.m_strdatadbpath)]
        for strfilename in strfilenames:
            strfilename = f"{strpath}/{strfilename}"
            self.loadDocuments(strfilename)
        # end for
        return True
    # end loadDocumentsFromDataDir()
    
    def loadDocuments(self, strtextfilename):
        try: 
            document_loader = TextLoader(strtextfilename)
            documents = document_loader.load()
            text_splitter = RecursiveCharacterTextSplitter(
                chunk_size=800,
                chunk_overlap=80,
                length_function=len,
                is_separator_regex=False,
            )
            chunks = text_splitter.split_documents(documents)
            self._addDocument(chunks)
            return True
        # end try  
        except Exception as e:
            logger.error(f"Error during load call: {e} with these settings: {self}")
            return False
        # end except
    # end loadDocuments()
    
    def clear(self):
        try:
            if os.path.exists(self.m_settings.m_strvectordbpath):
                shutil.rmtree(self.m_settings.m_strvectordbpath)
            return True
            # end if
        except Exception as e:
            logger.error(f"Error during clear call: {e} with these settings: {self}")
            return False
        # end except
    # end clearDatabase()
    
    def query(self, strquery: str, top_k=5):
        try:
            # Search the DB for resulting documents
            results = self.m_db.similarity_search_with_score(strquery, k=top_k)
            
            # return the resulting context of the query
            return "\n\n---\n\n".join([doc.page_content for doc, _score in results])    
        # end try
        except Exception as e:
            logger.error(f"Error during query call: {e} with these settings: {self}")
            return None
        # end except
    # end query()
           
    def _addDocument(self, chunks: list[Document]):
        # Calculate Page IDs.
        chunks_with_ids = self._calculateChunkIDs(chunks)

        # Add or Update the documents.
        existing_items = self.m_db.get(include=[])  # IDs are always included by default
        existing_ids = set(existing_items["ids"])

        # Only add documents that don't exist in the DB.
        new_chunks = []
        for chunk in chunks_with_ids:
            if chunk.metadata["id"] not in existing_ids:
                new_chunks.append(chunk)
            # end if
        # end for
        
        if len(new_chunks):
            print(f"👉 Adding new documents: {len(new_chunks)}")
            new_chunk_ids = [chunk.metadata["id"] for chunk in new_chunks]
            self.m_db.add_documents(new_chunks, ids=new_chunk_ids)
            #self.m_db.persist()
        # end if
        else:
            print("✅ No new documents to add")
        # end else
    # end _addDocument()
        
    def _calculateChunkIDs(self, chunks):
        # This will create IDs like "data/monopoly.pdf:6:2"
        # Page Source : Page Number : Chunk Index
        last_source_id = None
        current_chunk_index = 0
        for chunk in chunks:
            source = chunk.metadata.get("source")
            page = chunk.metadata.get("page", "NA")
            current_source_id = f"{source}:{page}"
        
            # If dealing with the same source/page, increment the chunk index
            if current_source_id == last_source_id:
                current_chunk_index += 1
            # end if
            else:
                current_chunk_index = 0
            # end else
            
            # Create the chunk ID (using source and index for text files)
            chunk_id = f"{source}:{current_chunk_index}" if page == "NA" else f"{source}:{page}:{current_chunk_index}"
            last_source_id = current_source_id
            
            # Add the chunk ID to the metadata
            chunk.metadata["id"] = chunk_id
        # end for
        return chunks
    # end calculateChunkIDs()
# end CVectorDatabase

#---------------------------------------------------------------------------------------
# name: loaddocs()
# desc: load the documents from the DATA_PATH to the db store in the CHROMA_PATH 
#---------------------------------------------------------------------------------------
def loaddocs():
    cvd = CVectorDatabase()
    cvd.create()
    cvd.load()
# end loaddocs()
    
#---------------------------------------------------------------------------------------
# name: querydocs()
# desc: query the documents in the DATA_PATH that are store in the CHROMA_PATH 
#---------------------------------------------------------------------------------------
def querydocs(strquery):
    cvd = CVectorDatabase()
    cvd.create()
    cvd.query(strquery)
# end querydocs()

#------------------------------------------------------------------------
# name: __main__
# desc: main entry point
#------------------------------------------------------------------------
if __name__ == "__main__":
    fire.Fire()
# end if