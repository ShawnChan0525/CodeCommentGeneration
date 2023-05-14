# coding=utf-8

import os
import random

import numpy as np
import torch
import torch.nn as nn
from transformers import RobertaConfig, RobertaModel, RobertaTokenizer

from iCommenter.ESALE_model.Seq2Seqs import Seq2Seq, Seq2Seq4unixcoder

def count_parameters(model):
    return sum(p.numel() for p in model.parameters() if p.requires_grad)



def set_seed(n_gpu, seed):
    """set random seed."""
    random.seed(seed)
    np.random.seed(seed)
    torch.manual_seed(seed)
    if n_gpu > 0:
        torch.cuda.manual_seed_all(seed)
        
def tokenize_input(inputs, max_seq, tokenizer, SOS = '<mask0>'):
    """
    inputs: str
    output: tensor"""
    inputs = tokenizer.tokenize(inputs)
    inputs = pad_input(inputs,max_seq,SOS)
    input_ids = tokenizer.convert_tokens_to_ids(inputs)
    input_ids = torch.tensor(input_ids, dtype=torch.long)
    return input_ids

def pad_input(arr: list, max_seq, SOS = '<mask0>'):
    if len(arr) > max_seq-2:
        arr = arr[:max_seq-2]           
        arr = [SOS] + arr + ['</s>']
    elif len(arr) == max_seq-2:
        arr = [SOS] + arr + ['</s>']
    else:
        arr = arr + ['</s>']
        padding = ["<pad>"]*(max_seq-len(arr)-1)
        arr.extend(padding)
        arr = [SOS] + arr
    return arr

def summarize2(input_seq: str, load_model_path: str):
    output = "sum a list of three numbers .,,return the sum of all the numbers in a list .,,sum of three numbers .,,sum three numbers "
    return output

def summarize(input_seq: str, load_model_path: str):
    
    #arguments
    model_name_or_path = "microsoft/unixcoder-base"
    load_model_path = load_model_path + ".bin"
    max_seq = 128
    max_output_seq = 128
    with_cuda = True
    cuda_devices = "1"
    beam_size = 5
    local_rank = -1
    seed = 42
    
    os.environ["CUDA_VISIBLE_DEVICES"] = cuda_devices
    # Setup CUDA, GPU & distributed training
    if local_rank == -1 or not with_cuda:
        device = torch.device(
            "cuda" if torch.cuda.is_available() and with_cuda else "cpu")
        n_gpu = torch.cuda.device_count()
    else:  # Initializes the distributed backend which will take care of sychronizing nodes/GPUs
        torch.cuda.set_device(local_rank)
        device = torch.device("cuda", local_rank)
        torch.distributed.init_process_group(backend="nccl")
        n_gpu = 1
    # Set seed
    set_seed(n_gpu, seed)

    tokenizer = RobertaTokenizer.from_pretrained(model_name_or_path, do_lower_case=True)
    config = RobertaConfig.from_pretrained(model_name_or_path)
    if model_name_or_path == "microsoft/unixcoder-base":
        config.is_decoder = True
    padding_id = tokenizer.convert_tokens_to_ids("<pad>")
    # budild model
    encoder = RobertaModel.from_pretrained(model_name_or_path,config = config)
    
    if model_name_or_path == "microsoft/codebert-base":
        decoder_layer = nn.TransformerDecoderLayer(d_model=config.hidden_size, nhead=config.num_attention_heads)
        decoder = nn.TransformerDecoder(decoder_layer, num_layers=6)
        model=Seq2Seq(encoder=encoder,decoder=decoder,config=config,
                  beam_size=beam_size, max_length=max_output_seq,
                  sos_id=tokenizer.cls_token_id,eos_id=tokenizer.sep_token_id, padding_id = padding_id)
    
    else:
        # in unixcoder, decoder == encoder
        model = Seq2Seq4unixcoder(encoder=encoder,decoder=encoder,config=config,
                  beam_size=beam_size,max_length=max_output_seq,
                  sos_id=tokenizer.convert_tokens_to_ids(["<mask0>"])[0],eos_id=tokenizer.sep_token_id)
    model.load_state_dict(torch.load(load_model_path))
    model = model.to(device)
    if n_gpu>1:
        model = nn.DataParallel(model,device_ids=cuda_devices)
    
    SOS = "<mask0>" if model_name_or_path == "microsoft/unixcoder-base" else "<s>"
    input_ids = tokenize_input(input_seq, max_seq, tokenizer, SOS)
    input_ids = input_ids.view(1,-1)
    input_ids = input_ids.to(device)
    
    # Calculate bleu
    model.eval()
    with torch.no_grad():
        preds = model(input_ids)
        for pred in preds:
            t = pred[0].cpu().numpy()
            t = list(t)
            if padding_id in t:
                t = t[:t.index(padding_id)]
            candidate = tokenizer.decode(
                t, clean_up_tokenization_spaces=False)
    return candidate

if __name__ == "__main__":
    # input_seq1 = "private static Frame showInitialSplash ( ) { Frame splashFrame = null ; Image image = null ; URL imageURL = getChosenSplashURL ( ) ; if ( imageURL != null ) { try { image = ImageIO . read ( imageURL ) ; } catch ( IOException e ) { e . printStackTrace ( ) ; } if ( image != null ) { splashFrame = AWTSplashWindow . splash ( image ) ; } } return splashFrame ; }"
    # input_seq2 = "private static Frame showInitialSplash ( ) { Frame splashFrame = null ; Image image = null ; URL imageURL = getChosenSplashURL ( ) ; if ( imageURL != null ) { try { image = ImageIO . read ( imageURL ) ; } catch ( IOException e ) { e . printStackTrace ( ) ; } if ( image != null ) { splashFrame = AWTSplash . splash ( image ) ; } } return splashFrame ; }"
    # models = ["ESALE_JCSD_ppl", "unixcoder_JCSD"]
    # for model in models:
    #     output_seq = ESALE(input_seq1, model)
    #     print(model + "(删前): " + output_seq)
    #     print(model + "(删后): " + ESALE(input_seq2, model))
    
    input_seq = '''def print(): print("Hello world!")'''
    output_seq = summarize(input_seq, "ESALE_model/ESALE_unixcoder_PCSD")
    print(output_seq)