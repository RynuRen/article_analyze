# def singleton(class_):
#   instances = {}
#   def getinstance(*args, **kwargs):
#     if class_ not in instances:
#         instances[class_] = class_(*args, **kwargs)
#     return instances[class_]
#   return getinstance



def singleton(class_):
    instances = {}

    def getinstance(*args, **kwargs):
        key = (class_, *args, tuple(kwargs.items()))
        if key not in instances:
            instances[key] = class_(*args, **kwargs)
        return instances[key]

    return getinstance