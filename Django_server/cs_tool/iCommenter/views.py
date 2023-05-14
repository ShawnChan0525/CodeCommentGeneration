import json
from django.http import HttpResponse,HttpRequest
from iCommenter.ESALE_model.ESALE import summarize2
from django.db.models.signals import post_save
from django.dispatch import receiver
from django.db.models import Avg
from .models import FeedbackInfo, ModelInfo

from iCommenter.models import Student,CommentInfo,FeedbackInfo,CodeInfo,ModelInfo
# Create your views here.

def generate(request):
    code = request.POST.get("code")
    comment = summarize2(code, "E:\Django_server\cs_tool\iCommenter\ESALE_model\ESALE_unixcoder_PCSD.bin")
    return HttpResponse(comment)
    
def store_comment(code, model, comment):
    code_record,_ = CodeInfo.objects.get_or_create(code=code)
    model_record,_ = ModelInfo.objects.get_or_create(model=model)
    comment_record,_ = CommentInfo.objects.get_or_create(code_id=code_record, model_id=model_record, comment=comment)
    return comment_record


def store_feedback(request: HttpRequest):
    req_dict = request.POST
    response_data = {"status": "", "message": ""}
    try:
        comment_record = store_comment(req_dict["code"],req_dict["model"],req_dict["comment"])
        FeedbackInfo.objects.create(comment_id = comment_record,feedback = req_dict["feedback"],score = req_dict["score"])
        update_model_score(comment_record.model_id)
        response_data = {"status": "success", "message": "Feedback stored successfully."}
    except Exception as e:
        response_data = {"status": "fail", "message": "Error: {}".format(e)}
    return HttpResponse(json.dumps(response_data), content_type="application/json")


def update_model_score(model_id: int):
    model = ModelInfo.objects.get(id=model_id)
    feedback_with_model = FeedbackInfo.objects.filter(model_id=model_id)
    average = feedback_with_model.aggregate(Avg('naturalness'),Avg('informativeness'),Avg('relevance'))
    final_average = sum(average.values()) / len(average)
    model.score = round(final_average, 2)
    model.save()