# Bagged Decision Trees for Classification
import pandas
import os
from sklearn import model_selection
from sklearn.ensemble import BaggingClassifier
from sklearn.tree import DecisionTreeClassifier
from sklearn.metrics import accuracy_score
from sklearn.metrics import precision_score
from sklearn.metrics import recall_score
from sklearn.metrics import roc_curve
from sklearn.preprocessing import label_binarize
from sklearn.metrics import auc
from sklearn.metrics import confusion_matrix
import pickle
import matplotlib.pyplot as plt
from scipy import interp
from itertools import cycle
import numpy as np


#Function to apply the training with the ensemble bagged tree, we use parameter tuned on previous research projects
#like seed and num_trees .
def train_bagging(train_data_file, names):
    dataframe = pandas.read_csv(open(train_data_file, 'r', encoding='utf8'), names=names)
    array = dataframe.values
    # Train data without labes
    X = array[:, 0:len(names)-1]
    # Train data labels
    Y = array[:, len(names) - 1]
    # Model parameters and constructor
    seed = 7
    cart = DecisionTreeClassifier()
    num_trees = 12
    model = BaggingClassifier(base_estimator=cart, n_estimators=num_trees, random_state=seed)
    # Model generation fitting training data and labels
    model.fit(X, Y)
    return model


def train_bagging_movile_test(train_data_file, names):
    dataframe = pandas.read_csv(open(train_data_file, 'r', encoding='utf8'), names=names)
    array = dataframe.values
    # Train data without labes
    X = array[:, 0:3]
    # Train data labels
    Y = array[:, len(names) - 1]
    # Model parameters and constructor
    seed = 7
    cart = DecisionTreeClassifier()
    num_trees = 12
    model = BaggingClassifier(base_estimator=cart, n_estimators=num_trees, random_state=seed)
    # Model generation fitting training data and labels
    model.fit(X, Y)
    return model

def test_bagging(test_data_file, estimator, names):
    data_frame = pandas.read_csv(open(test_data_file, 'r', encoding='utf8'), names=names)
    array = data_frame.values
    # Test data without labels
    X = array[:, 0:len(names) - 1]
    y_test = array[:, len(names) - 1]
    # Estimation of labels with the model
    Y = estimator.predict(X)
    seed = 7
    kfold = model_selection.KFold(n_splits=10, random_state=seed)
    results = model_selection.cross_val_score(estimator, X, Y, cv=kfold)
    y_score = estimator.predict_proba(X)
    # Validation with result
    accuracyscore, F1, pscore, rscore = metrics(y_test, Y)
    print(str(accuracyscore))
    print(str(F1))
    print(str(pscore))
    print((rscore))

    # Roc curve area
    cm, curve_area = compute_roc_curve(Y, y_score, y_test)
    bus_cm = cm[1][1]
    metro_cm = cm[3][3]
    train_cm = cm[7][7]
    tram_cm = cm[8][8]

    mean = (bus_cm + metro_cm + train_cm + tram_cm + F1 + curve_area["macro"] + accuracyscore) / 7
    return Y, results.mean(), accuracyscore, F1, pscore, rscore, mean , cm


def compute_roc_curve(Y, y_score, y_test):
    curve_area = compute_roc(y_test, y_score)
    cm = confusion_matrix(y_test, Y)
    cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]
    plot_confusion_matrix(y_test, Y)
    plt.show()
    return cm, curve_area

# Function to compute metrics of the model , accuracy , precision score, recall score
# and f1 score
def metrics(x, y):
    accuracyscore = accuracy_score(x, y)
    preccisionscore = precision_score(x, y, average='weighted')
    recallscore = recall_score(x, y, average='weighted')
    f1 = 2 * (preccisionscore * recallscore) / (preccisionscore + recallscore)
    return accuracyscore, f1, preccisionscore, recallscore


def compute_roc(y, y_score):
    # Necessito un
    # Binarize the output

    y = label_binarize(y,
                        classes=['Bicycle', 'Bus', 'Car', 'Metro',
                                 'Motorbike', 'Run', 'Stationary', 'Train', 'Tram', 'Walk'])
    n_classes = y.shape[1]
    fpr = dict()
    tpr = dict()
    roc_auc = dict()
    for i in range(n_classes):
        fpr[i], tpr[i], _ = roc_curve(y[:, i], y_score[:, i])
        roc_auc[i] = auc(fpr[i], tpr[i])

    # Compute micro-average ROC curve and ROC area
    fpr["micro"], tpr["micro"], _ = roc_curve(y.ravel(), y_score.ravel())
    roc_auc["micro"] = auc(fpr["micro"], tpr["micro"])

    return plot_curves(fpr, tpr, n_classes, 2, roc_auc)


def plot_confusion_matrix(y_true, y_pred,
                          normalize=False,
                          title=None,
                          cmap=plt.cm.Blues):
    """
    This function prints and plots the confusion matrix.
    Normalization can be applied by setting `normalize=True`.
    """
    if not title:
        if normalize:
            title = 'Normalized confusion matrix'
        else:
            title = 'Confusion matrix, without normalization'

    # Compute confusion matrix
    cm = confusion_matrix(y_true, y_pred)
    # Only use the labels that appear in the data
    classes = ['Bicycle', 'Bus', 'Car', 'Metro',
                                 'Motorbike', 'Run', 'Stationary', 'Train', 'Tram', 'Walk']
    if normalize:
        cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]
        print("Normalized confusion matrix")
    else:
        print('Confusion matrix, without normalization')

    print(cm)

    fig, ax = plt.subplots()
    im = ax.imshow(cm, interpolation='nearest', cmap=cmap)
    ax.figure.colorbar(im, ax=ax)
    # We want to show all ticks...
    ax.set(xticks=np.arange(cm.shape[1]),
           yticks=np.arange(cm.shape[0]),
           # ... and label them with the respective list entries
           xticklabels=classes, yticklabels=classes,
           title=title,
           ylabel='True label',
           xlabel='Predicted label')

    # Rotate the tick labels and set their alignment.
    plt.setp(ax.get_xticklabels(), rotation=45, ha="right",
             rotation_mode="anchor")

    # Loop over data dimensions and create text annotations.
    fmt = '.2f' if normalize else 'd'
    thresh = cm.max() / 2.
    for i in range(cm.shape[0]):
        for j in range(cm.shape[1]):
            ax.text(j, i, format(cm[i, j], fmt),
                    ha="center", va="center",
                    color="white" if cm[i, j] > thresh else "black")
    fig.tight_layout()
    return ax


def plot_curves(fpr, tpr, n_classes, lw , roc_auc):

    # Compute macro-average ROC curve and ROC area

    # First aggregate all false positive rates
    all_fpr = np.unique(np.concatenate([fpr[i] for i in range(n_classes)]))

    # Then interpolate all ROC curves at this points
    mean_tpr = np.zeros_like(all_fpr)
    for i in range(n_classes):
        mean_tpr += interp(all_fpr, fpr[i], tpr[i])

    # Finally average it and compute AUC
    mean_tpr /= n_classes

    fpr["macro"] = all_fpr
    tpr["macro"] = mean_tpr
    roc_auc["macro"] = auc(fpr["macro"], tpr["macro"])
    # Plot all ROC curves
    plt.figure()
    plt.plot(fpr["micro"], tpr["micro"],
             label='micro-average ROC curve (area = {0:0.2f})'
                   ''.format(roc_auc["micro"]),
             color='deeppink', linestyle=':', linewidth=4)

    plt.plot(fpr["macro"], tpr["macro"],
             label='macro-average ROC curve (area = {0:0.2f})'
                   ''.format(roc_auc["macro"]),
             color='navy', linestyle=':', linewidth=4)

    colors = cycle(['aqua', 'darkorange', 'cornflowerblue'])
    for i, color in zip(range(n_classes), colors):
        plt.plot(fpr[i], tpr[i], color=color, lw=lw,
                 label='ROC curve of class {0} (area = {1:0.2f})'
                       ''.format(i, roc_auc[i]))

    plt.plot([0, 1], [0, 1], 'k--', lw=lw)
    plt.xlim([0.0, 1.0])
    plt.ylim([0.0, 1.05])
    plt.xlabel('False Positive Rate')
    plt.ylabel('True Positive Rate')
    plt.title('Some extension of Receiver operating characteristic to multi-class')
    plt.legend(loc="lower right")
    plt.show()
    return roc_auc


#If previos value dosn't exist we will return -1 that means you we must save model and create a file with actual acc
def compare_results_with_previous(path, new_accuracy):
    try:
        accuracy_x = open(path, "r")
    except OSError:
        return -1
    accuracy_value = float(accuracy_x.read())
    if new_accuracy >= accuracy_value:
        return 1
    else:
        return 0


def get_version_name(path):
    try:
        path_to_file = ""
        for file in os.listdir(path):
            if file.endswith(".sav"):
                path_to_file = file

        n = str(os.path.basename(path_to_file).split("V")[1].split(".")[0])

        return 'modelV' + n + '.sav'
    except IndexError:
        return 'modelV0.sav'


def generate_version_name(path):
    try:
        path_to_file = ""
        for file in os.listdir(path):
            if file.endswith(".sav"):
                path_to_file = file

        actual_version = os.path.basename(path_to_file).split("V")[1].split(".")[0]
    except IndexError:
        actual_version = 0

    new_version = int(actual_version) + 1
    return 'modelV' + str(new_version)+'.sav'


def ensemble_bagged_trees(input_path, model_path, accuracy_path, trained_data_path, test_data_path , confusion_matrix_path):
    '''structure_names = ['Wmean_accx', 'Wmean_accy', 'Wmean_accz', 'Wmean_acclnx', 'Wmean_acclny', 'Wmean_acclnz',
                       'Wstd_accx'
        , 'Wstd_accy', 'Wstd_accz', 'Wstd_acclnx', 'Wstd_acclny', 'Wstd_acclnz', 'Wpca1_accx', 'Wpca1_accy',
                       'Wpca1_accz'
        , 'Wpca1_acclnx', 'Wpca1_acclny', 'Wpca1_acclnz', 'activity']'''

    structure_names = ['Wmean_accx', 'Wmean_accy', 'Wmean_accz' , 'activity']
    files_location = input_path
    model_file = model_path + get_version_name(files_location)
    file_accuracy = accuracy_path
    file_confusion = confusion_matrix_path
    traindata = trained_data_path
    test_data = test_data_path

    # Train model
    model_bagging = train_bagging_movile_test(files_location + traindata, structure_names)
    predictions, accuracy, accuracy_metric, f1_metric, precision_metric, recall_metric , mean_metric , confusion_matrix = test_bagging(files_location + test_data, model_bagging, structure_names)
    code = compare_results_with_previous(files_location + file_accuracy, mean_metric)
    if code == 0:
        print("The model was updated")
        file_to_save_model_to = 'models/results/' + generate_version_name(files_location )
        pickle.dump(model_bagging, open(files_location + file_to_save_model_to, 'wb'))
        accuracy_file = open(files_location + file_accuracy, 'w+')
        accuracy_file.write(str(mean_metric))


    elif code == -1:
        print("First model generated")
        pickle.dump(model_bagging, open(files_location + model_file, 'wb'))
        accuracy_file = open(files_location + file_accuracy, 'w+')
        accuracy_file.write(str(mean_metric))

    accuracy_file = open(files_location + file_accuracy, 'w+')
    accuracy_file.write(str(mean_metric))
    confusion_matrix_file = open(files_location + file_confusion, 'w+')
    confusion_matrix_file.write(str(confusion_matrix))

# Change path's depending on your filename structure to test that script
ensemble_bagged_trees(input_path=r'/mobilitatApp-backend/Python/',
                                               model_path='models/', accuracy_path='models/results/model_acc.txt',
                                               trained_data_path='data/processed/Train_data.csv',
                                               test_data_path='data/processed/Test_data.csv',
                                               confusion_matrix_path='models/results/confusion_matrix.txt')



