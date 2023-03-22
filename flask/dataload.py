import pickle
import concurrent.futures
from sklearn.metrics.pairwise import cosine_similarity

def open_pickle(key):
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
            bin_ = executor.submit(open_pickle, f)
            futures.append(bin_)
    print(f"--(load): {time.time() - start}")

    id_list = []
    dtm_list = []
    
    for fut in futures:
        if not fut.result():
            continue
        id_, dtm_part = fut.result()
        id_list.append(id_[0])
        dtm_list.append(dtm_part)

    start = time.time()
    cos_sim_res = []
    for dtm_part in dtm_list:
        cos_sim = cosine_similarity(tfidf_content, dtm_part)[0]
        cos_sim_res.append(cos_sim.tolist()[0])
    print(f"--(calc): {time.time() - start}")
    return id_list, cos_sim_res