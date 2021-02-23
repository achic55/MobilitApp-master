import pandas as pd
import os
import fnmatch
import numpy as np
import datetime
import shutil


# We iterate over data/raw if file has ACC name we add it do a new line on the DataFrame,
# Acceleration file syntax
MAG__CSV = '_MAG*.csv'
ACC__CSV = '*_ACC*.csv'
LABELS_T_CSV = 'labels_t.csv'
ACCLNZ_T_CSV = 'acclnz_t.csv'
ACCLNY_T_CSV = 'acclny_t.csv'
ACCLNX_T_CSV = 'acclnx_t.csv'
ACCZ_T_CSV = 'accz_t.csv'
ACCY_T_CSV = 'accy_t.csv'
ACCX_T_CSV = 'accx_t.csv'

# Function that take a path and search for a acceleration tag on the file name , and concatenate the data to the
# generic acceleration file data .

# Testing function to introduce magnetometer data (NOT FINISHED)
def mag_generation(trip_identifiers, samples, interval, path):
    # Generation of Dataframe with all magnetometer records of each trip
    magx = pd.DataFrame()
    magy = pd.DataFrame()
    magz = pd.DataFrame()
    mag_x_series = []
    mag_y_series = []
    mag_z_series = []
    for trip in trip_identifiers:
        ident = trip
        samples = trip.value
        all_mag_df = pd.DataFrame()
        for file in os.listdir(path):
            if fnmatch.fnmatch(file, ident + MAG__CSV) and os.path.getsize(path + file) > 0:
                df = pd.read_csv(path + '/' + file)
                all_mag_df = all_mag_df.append(df)
        # Use Pandas Series and scipy interpolation to generate final data
        if len(all_mag_df) > 0:
            mag_x_series = pd.Series(all_mag_df.iloc[:, 1])
            mag_y_series = pd.Series(all_mag_df.iloc[:, 2])
            mag_z_series = pd.Series(all_mag_df.iloc[:, 3])
            x = np.linspace()

    return magx, magy, magz

# Function that take a path and search for a acceleration tag on the file name , and concatenate the data to the
# generic acceleration file data .
def acc_generation(path, interval):
    # TODO : Utils with path validation
    accx = pd.DataFrame()
    accy = pd.DataFrame()
    accz = pd.DataFrame()
    labels = pd.DataFrame()
    for file in os.listdir(path):
        if fnmatch.fnmatch(file, ACC__CSV) and os.path.getsize(path + file) > 0:
            df = pd.read_csv(path + '/' + file)
            samples = df.iloc[:, 1].values.size
            num_splits = round(samples / interval)
            in_words = file.split('_')
            for n in range(0, num_splits):
                labels = labels.append(pd.Series(in_words[2]), ignore_index=True)
                x = df.iloc[(n * interval): ((n + 1) * interval), 1].values
                y = df.iloc[(n * interval): ((n + 1) * interval), 2].values
                z = df.iloc[(n * interval): ((n + 1) * interval), 3].values
                accx = accx.append(pd.Series(x), ignore_index=True)
                accy = accy.append(pd.Series(y), ignore_index=True)
                accz = accz.append(pd.Series(z), ignore_index=True)
    return accx, accy, accz, labels


# Apply napierian logarithm
def acc_ln_generation(x, y, z):
    ln_x = x.applymap(np.log)
    ln_y = y.applymap(np.log)
    ln_z = z.applymap(np.log)
    return ln_x, ln_y, ln_z


# Functionality to move the files to construct the raw  data set,
def generate_raw_dataset(path, out_path, initial_year=datetime.datetime.now().year,
                         ending_year=datetime.datetime.now().year, max_files=-1):
    years = range(initial_year, ending_year + 1)
    numfilesloaded = 0
    for year in years:
        if os.path.exists(path + '/' + str(year)):
            print(year)
            files = os.listdir(path + '/' + str(year))
            filesize = len(files)
            i = 0
            while max_files != numfilesloaded and i < filesize:
                if fnmatch.fnmatch(files[i], '*.csv'):
                    shutil.copy(path + '/' + str(year) + '/' + str(files[i]), out_path)
                    numfilesloaded = numfilesloaded + 1
                i = i + 1

# Removes files stored from previous executions on the data/raw folder
def clean_raw_data(path):
    for file in os.listdir(path):
        os.remove(path=path + '/' + file)


def write_output_files(base_path, df_acc_x, df_acc_y, df_acc_z, df_labels, df_ln_acc_x, df_ln_acc_y, df_ln_acc_z):
    df_acc_x.to_csv(base_path + ACCX_T_CSV, sep=',', encoding='utf-8',
                    index=False, header=False)
    df_acc_y.to_csv(base_path + ACCY_T_CSV, sep=',', encoding='utf-8',
                    index=False, header=False)
    df_acc_z.to_csv(base_path + ACCZ_T_CSV, sep=',', encoding='utf-8',
                    index=False, header=False)
    df_ln_acc_x.to_csv(base_path + ACCLNX_T_CSV, sep=',', encoding='utf-8',
                       index=False, header=False)
    df_ln_acc_y.to_csv(base_path + ACCLNY_T_CSV, sep=',', encoding='utf-8',
                       index=False, header=False)
    df_ln_acc_z.to_csv(base_path + ACCLNZ_T_CSV, sep=',', encoding='utf-8',
                       index=False, header=False)
    df_labels.to_csv(base_path + LABELS_T_CSV, sep=',', encoding='utf-8',
                     index=False, header=False)

# Starting point of the Data Treatment scrip, this script will take as input the raw_data stored on the server system
# and generate as output the separated files with all acceleation axis on a specific range of years and the log of this
# data


def data_treatment(base_path, input_path, root_path, init_year, end_year, mx_files):
    clean_raw_data(path=input_path)
    generate_raw_dataset(path=root_path,
                         out_path=input_path,
                         initial_year=init_year, ending_year=end_year, max_files=mx_files)
    df_acc_x, df_acc_y, df_acc_z, df_labels = acc_generation(input_path, 400)
    df_ln_acc_x, df_ln_acc_y, df_ln_acc_z = acc_ln_generation(df_acc_x, df_acc_y, df_acc_z)

    write_output_files(base_path, df_acc_x, df_acc_y, df_acc_z, df_labels, df_ln_acc_x, df_ln_acc_y, df_ln_acc_z)


# Change input path depending on your system stucture
data_treatment(base_path='/mobilitatApp-backend/Python/data/interim/',
               input_path='/mobilitatApp-backend/Python/data/raw/',
               root_path='/oldsd/pi/', init_year=2014, end_year=2019, mx_files=-1)
