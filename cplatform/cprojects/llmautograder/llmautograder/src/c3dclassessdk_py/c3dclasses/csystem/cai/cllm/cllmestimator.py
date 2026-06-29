import numpy as np  # Import NumPy for array operations
from sklearn.base import BaseEstimator, ClassifierMixin

from sklearn.model_selection import GridSearchCV, KFold
from sklearn.datasets import make_classification  # Optional for generating synthetic data
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split

import numpy as np
from sklearn.base import BaseEstimator, ClassifierMixin
from sklearn.model_selection import GridSearchCV
from sklearn.metrics import accuracy_score

class CFunctionParameterTunerEstimator(BaseEstimator, ClassifierMixin):
    def __init__(self, factor=100):
        self.factor = factor
    
    def fit(self, X, y):
        self.classes_ = np.unique(y)
        return self
    
    def predict(self, X):
        YPredictionsList = [x * self.factor for x in X]
        return np.array(YPredictionsList)
    
    def score(self, X, y):
        return accuracy_score(y, self.predict(X))

def main():
    # Generate a larger dataset with 30 samples
    X = np.array([[i] for i in range(1, 31)])  # 2D array as required
    y = np.array([2 * i for i in range(1, 31)])  # Corresponding y values

    param_grid = {'factor': [10, 2, 1, 4, 6, 7]}
   
    kf = KFold(n_splits=3, shuffle=True, random_state=42)  # Add shuffling
   
    gscv = GridSearchCV(
        estimator=CFunctionParameterTunerEstimator(),
        param_grid=param_grid,
        scoring='accuracy',
        cv=kf,  # Use 3-fold cross-validation
        verbose=1
    )
    
    gscv.fit(X, y)
    
    print("Best Parameters:", gscv.best_params_)
    print("Best Cross-Validation Score:", gscv.best_score_)

    
if __name__ == "__main__":
    main()


#-------------------------------------------------------------------------
# name: CFunctionParameterTuner 
# desc: defines an object to determine the right parameters to tun
#-------------------------------------------------------------------------
class CFunctionParameterTunerBase:
    def __init__(self):
        self.m_fnInitParams = None    # describes the function to init the parameters
        self.m_fnTuneParams = None    # describes the function to tune the parameters
        self.m_listX = None           # list containing all of the input of the function
        self.m_listY = None           # list containing the expected output of the function given the list of input X
        self.m_dictP = None           # list of parameters to tune
        self.m_gscv = None            # grid search estimator
        return
    # end __init__()
    
    def create(self):
        self.m_cfpte = CFunctionParameterTunerEstimator(self)  
        return True
    # end create()
    
    def setTuneFunction(self, func):
        self.m_fnTuneParams = func
    # end setFunction()
    
    def setInitFunction(self, func):
        self.m_fnInitParams = func
    # end setFunction()
    
    def doTuning(self):
        self.m_gscv = GridSearchCV(
            estimator=self.m_cfpte,
            param_grid=self.m_dictP,
            scoring='accuracy',
            cv=2,
            verbose=1
        )        
        self.m_gscv.fit(self.m_listX, self.m_listY)
    # end doTuning()
    
    def setX(self, X):
        self.m_listX = X
    # end setX()
    
    def setY(self, Y):
        self.m_listY = Y
    # end setY()
    
    def setP(self, P):
        self.m_dictP = P
    # end setP()
    
    def initParameters(self, estimator):
        self.m_fnInitParams(estimator)
    # end initParameters()
    
    def tuneParameters(self, estimator):
        self.m_fnTuneParams(estimator)
    # end tuneParameters()
    
    def getBestParameters(self):
        self.m_gscv.best_params_
    # end getBestParameters()
    
    def getBestScore(self):
        self.m_gscv.best_score_
    # end getBestScore()
# end CFunctionParameterTuner

def initParams(P):
    print("initParams()")
    P.factor = 10
# end 
def tuneParams(P, Xlist):
    print("tuneParams()")
    YPredictionslist = [x * P.factor for x in Xlist]
    return np.array(YPredictionslist)
# end findfactor


#-------------------------------------------------------------
# name: main()
# desc: test the CLLM class
#-------------------------------------------------------------
def main():
#    cfpt = CFunctionParameterTuner()
#    cfpt.setInitFunction(initParams)
#    cfpt.setTuneFunction(tuneParams)
#    cfpt.setX([1,2,3,4,5,6])
#    cfpt.setY([2,4,6,8,10,12])
#    cfpt.setP({'factor':[10,2,1,4,6,7]})
#    cfpt.create()
#    cfpt.doTuning()
 
 
    X = [1,2,3,4,5,6]
    Y = [2,4,6,8,10,12]
    P = {'factor':[10,2,1,4,6,7]}
    
    gscv = GridSearchCV(
        estimator=CFunctionParameterTunerEstimator(),
        param_grid=P,
        scoring='accuracy',
        cv=3,
        verbose=1
    )        
    
    gscv.fit(X, Y)
    
    print(gscv.best_params_)
    print(gscv.best_score_)
# end main()

# Example usage
if __name__ == "__main__":
    main()
# end __main__