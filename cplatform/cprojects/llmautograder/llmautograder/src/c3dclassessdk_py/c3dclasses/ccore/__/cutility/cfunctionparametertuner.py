#------------------------------------------------------------------------
# file: cfunctionparametertuneestimatorbase.py 
# desc: custom estimator definition for tuning general purpose functions 
#-------------------------------------------------------------------------
import numpy as np
from sklearn.base import BaseEstimator, ClassifierMixin
from sklearn.model_selection import GridSearchCV, KFold
from sklearn.metrics import accuracy_score

#-----------------------------------------------------------------------------------
# name: CFunctionParameterTunerEstimatorBase 
# desc: custom estimator definition for tuning general purpose functions 
#-----------------------------------------------------------------------------------
class CFunctionParameterTunerEstimatorBase(BaseEstimator, ClassifierMixin):
    def __init__(self):
        self.m_listX = None           # list containing all of the input of the function
        self.m_listY = None           # list containing the expected output of the function given the list of input X
        self.m_dictP = None           # list of parameters to tune
        self.m_estimator = None       # grid search estimator
    # end __init__()

    def setX(self, X):
        self.m_listX = X
    # end setX()
    
    def setY(self, Y):
        self.m_listY = Y
    # end setY()
    
    def setP(self, P):
        self.m_dictP = P
    # end setP()
   
    def getBestParameters(self):
        return self.m_estimator.best_params_
    # end getBestParameters()
    
    def getBestScore(self):
        return self.m_estimator.best_score_
    # end getBestScore()    
    
    def estimate(self): 
        # Use K-Fold cross-validation with shuffling for better distribution
        kf = KFold(n_splits=3, shuffle=True, random_state=42)

        # Initialize GridSearchCV
        self.m_estimator = GridSearchCV(
            estimator=self,
            param_grid=self.m_dictP,
            scoring='accuracy',
            cv=kf,  # Custom cross-validation strategy
            verbose=1
        )
    
        # Fit the grid search to the data
        self.m_estimator.fit(self.m_listX, self.m_listY)
        return    
    # end estimate()
   
    ################################################################################################
    # helper methods - should not be called directly - implement these in the inherited class
    ################################################################################################
    
    #-----------------------------------------------
    # name: fit()
    # desc: 
    #-----------------------------------------------
    def fit(self, X, y):
        self.classes_ = np.unique(y)
        return self
    # end fit()
    
    #-----------------------------------------------
    # name: predict()
    # desc:
    #-----------------------------------------------
    def predict(self, X):
        # Ensure X is flattened for calculations
        X_flat = np.ravel(X)  
        
        YPredictionsList = [self.tune(x) for x in X_flat]
        return np.array(YPredictionsList)
    # end predict()
    
    #-----------------------------------------------
    # name: score()
    # desc:
    #-----------------------------------------------
    def score(self, X, y):
        # Simplified scoring based on binary accuracy
        return accuracy_score(y, self.predict(X))
    # end score()
    
    #----------------------------------------------------------------
    # name: tune()
    # desc: method or function to implement to do the tuning
    #----------------------------------------------------------------
    def tune(self, x):
        y = x
        return y
    # end tune()
    
# end CFunctionParameterTunerEstimatorBase

#---------------------------------------------------------
# name: CMyFunctionParameterTunerEstimator 
# desc: custom estimator
#---------------------------------------------------------
class CMyFunctionParameterTunerEstimator(CFunctionParameterTunerEstimatorBase):
    def __init__(self, factor = 10):
        self.factor = factor
        super().__init__()
    # end __init__()

    def tune(self, x):
        # take an instance of x and parameters/factors to determine an instance of y 
        y = x * self.factor
        return y 
    # end funcToTune()
# end CMyFunctionParameterTunerEstimator()

#----------------------------------------------------------
# name: main()
# desc: Main function for running GridSearchCV
#----------------------------------------------------------
def main():
    mfpte = CMyFunctionParameterTunerEstimator()
    mfpte.setX([1,2,3,4,5,6,7,8,9,10])
    mfpte.setY([2,4,6,8,10,12,14,16,18,20])
    mfpte.setP({'factor': [2, 3, 5, 10, 20, 50, 100]})
    mfpte.estimate()
    print("Best Parameters:",mfpte.getBestParameters())
    print("Best Cross-Validation Score:", mfpte.getBestScore())
# end main()

#----------------------------------------------------------
# name: __main__()
# desc: main entry point
#----------------------------------------------------------
if __name__ == "__main__":
    main()
# end if