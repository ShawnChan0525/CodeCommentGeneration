from django.db import models

# Create your models here.

class Student(models.Model):
    class Meta:
        db_table = "Student"
        
class CodeInfo(models.Model):
    id = models.AutoField(primary_key=True)
    code = models.TextField(max_length=2000, unique= True)
    class Meta:
        db_table = "CodeInfo"
        
        
class ModelInfo(models.Model):
    id = models.AutoField(primary_key=True)
    model = models.CharField(max_length=20, unique= True)
    # score = models.FloatField(max_digits=3,decimal_places=2)
    score = models.FloatField()
    class Meta:
        db_table = "ModelInfo"
    
class CommentInfo(models.Model):
    id = models.AutoField(primary_key=True)
    code_id = models.ForeignKey(CodeInfo, on_delete= models.CASCADE)
    model_id = models.ForeignKey(ModelInfo, on_delete= models.CASCADE)
    comment = models.TextField(max_length=2000)
    
    class Meta:
        db_table = "CommentInfo"
        
class FeedbackInfo(models.Model):
    id = models.AutoField(primary_key=True)
    comment_id = models.ForeignKey(CommentInfo,on_delete= models.CASCADE)
    feedback = models.TextField(max_length=2000)
    score = models.PositiveSmallIntegerField()
    class Meta:
        db_table = "FeedbackInfo2"
        
class SuggestionInfo(models.Model):
    id = models.AutoField(primary_key=True)
    
    
    class Meta:
        db_table = "SuggestionInfo"