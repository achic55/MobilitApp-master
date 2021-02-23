# Bagged Decision Trees for Classification
import pandas
from sklearn import model_selection
#from sklearn.model_selection import KFold
from sklearn.ensemble import BaggingClassifier
from sklearn.tree import DecisionTreeClassifier
import pickle


def trainBagging(traindatafile, file):
    names = ['Wmean_accx', 'Wmean_accy', 'Wmean_accz', 'Wmean_acclnx', 'Wmean_acclny', 'Wmean_acclnz','Wstd_accx', 'Wstd_accy', 'Wstd_accz', 'Wstd_acclnx', 'Wstd_acclny',
             'Wstd_acclnz','Wpca1_accx', 'Wpca1_accy', 'Wpca1_accz','Wpca1_acclnx', 'Wpca1_acclny', 'Wpca1_acclnz', 'activity']
    dataframe = pandas.read_csv(open(traindatafile, 'r',encoding='utf8'), names=names)
    array = dataframe.values
    X = array[:, 0:len(names)-1]
    Y = array[:, len(names)-1]
    seed = 7
    kfold = model_selection.KFold(n_splits=10, random_state=seed)
    cart = DecisionTreeClassifier()
    #We choose 12 trees in base of best results on tests with multiple number of trees
    num_trees = 12
    model = BaggingClassifier(base_estimator=cart, n_estimators=num_trees, random_state=seed)
    results = model_selection.cross_val_score(model, X, Y, cv=kfold)
    model.fit(X, Y)
    compare_metrics()
    print("Accuracy: " + str(results.mean()))
    # save the model to disk
    pickle.dump(model, open(filetosavemodelto, 'wb'))
    return model


#TODO: Cambiar estas funciones con el nuevo codigo
def save_metrics(metrics, file):
    metric = pandas.DataFrame(data=metrics)
    metric.to_csv(file)


def compare_metrics(results, file):
    #TODO: Añadir nombreas al csv con las metricas para hacerlo mas ordenado
    data_frame_actual_metrics = pandas.read_csv(open(file, 'r', encoding='utf8'))
    origin_results = data_frame_actual_metrics
    if True:
        save_metrics(results, file)

# filetosavemodelto = os.path.join(os.path.dirname(__file__), "python_finalized_model.sav")
# traindata = os.path.join(os.path.dirname(__file__), "traindata_noheader1.csv")
files_location = r'/home/mobilitat/2018/Python/'
filetosavemodelto = 'models/model.sav'
traindata = 'processed/Train_data.csv'
file_to_save_metrics = 'processed/metrics.csv'
# Train model
trainBagging(files_location + traindata, files_location + filetosavemodelto, file_to_save_metrics)
