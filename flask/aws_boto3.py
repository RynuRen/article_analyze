import boto3
import pickle
import concurrent.futures
from sklearn.metrics.pairwise import cosine_similarity

# def open_s3(key):
#     try:
#         s3 = boto3.resource('s3')
#         bucket = s3.Bucket("ssac-2nd-team-mint")
#         load_data = pickle.loads((bucket).Object(key).get()['Body'].read())
#         return load_data
#     except:
#         return False

def open_s3(key):
    try:
        with open(key, 'rb') as f:
            bin_ = pickle.load(f)
        return bin_
    except:
        return False

def read_parallel(tfidf_content, path_list):
    import time
    start = time.time()
    with concurrent.futures.ThreadPoolExecutor() as executor:
        futures = []
        for f in path_list:
            bin_ = executor.submit(open_s3, f)
            if not bin_:
                continue
            futures.append(bin_)
    print(f"--불러오기 시간: {time.time() - start}")

    id_list = []
    dtm_list = []
    
    for fut in futures:
        id_, dtm_part = fut.result()
        id_list.append(id_[0])
        dtm_list.append(dtm_part)

    start = time.time()
    cos_sim_res = []
    for dtm_part in dtm_list:
        cos_sim = cosine_similarity(tfidf_content, dtm_part)[0]
        cos_sim_res.append(cos_sim.tolist()[0])
    print(f"--연산 시간: {time.time() - start}")
    return id_list, cos_sim_res

    # start = time.time()
    # with concurrent.futures.ThreadPoolExecutor() as executor:
    #     cos_sim_res = []
    #     for dtm_part in dtm_list:
    #         cos_sim = executor.submit(cosine_similarity, tfidf_content, dtm_part)
    #         cos_sim_res.append(cos_sim.result()[0].tolist()[0])
    # print(f"연산 시간: {time.time() - start}")
    # return id_list, cos_sim_res

# with open("pickled_tfidf_vec.bin", "rb") as f:
#     tfidf_vec = pickle.load(f)