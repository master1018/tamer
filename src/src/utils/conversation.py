from random import randrange

import streamlit as st
from openai.error import InvalidRequestError, OpenAIError
from streamlit_chat import message

from .agi.chat_gpt import create_gpt_completion
from .stt import show_voice_input
from .tts import show_audio_player


def clear_chat() -> None:
    st.session_state.generated = []
    st.session_state.past = []
    st.session_state.messages = []
    st.session_state.user_text = [None, None]
    st.session_state.seed = randrange(10**8)  # noqa: S311
    st.session_state.costs = []
    st.session_state.total_tokens = []

def upload_src():
    if st.session_state.user_text[0] == None:
        st.error("无效上传")
        return 
    fn = "src" + st.session_state.code_type
    fp = open(fn, "w")
    fp.write(st.session_state.user_text[0])
    fp.close
    st.success("上传源文件成功")
    st.session_state.user_text[0] = None

def upload_dst():
    if st.session_state.user_text[1] == None:
        st.error("无效上传")
        return 
    fn = "dst" + st.session_state.code_type
    fp = open(fn, "w")
    fp.write(st.session_state.user_text[1])
    fp.close
    st.success("上传待检测文件成功")
    st.session_state.user_text[1] = None


def show_text_input() -> None:
    upload_src_file = st.file_uploader("源文件", accept_multiple_files=False)
    upload_dst_file = st.file_uploader("待检测文件", accept_multiple_files=False)
    #st.text_area(label=st.session_state.locale.chat_placeholder, value=st.session_state.user_text, key="user_text")
    if upload_src_file is not None:
        st.session_state.user_text[0] = upload_src_file.getvalue().decode("utf-8")
        upload_src_file = None
    if upload_dst_file is not None:
        st.session_state.user_text[1] = upload_dst_file.getvalue().decode("utf-8")
        upload_dst_file = None

def get_user_input():
    match st.session_state.input_kind:
        case st.session_state.locale.input_kind_1:
            show_text_input()
        case st.session_state.locale.input_kind_2:
            show_voice_input()
        case _:
            show_text_input()


def show_chat_buttons() -> None:
    b0, b1, b2 = st.columns(3)
    with b0, b1, b2:
        b0.button(label=st.session_state.locale.chat_run_btn, on_click=upload_src)
        b1.button(label=st.session_state.locale.chat_clear_btn, on_click=upload_dst)
        b2.download_button(
            label=st.session_state.locale.chat_save_btn,
            data="\n".join([str(d) for d in st.session_state.messages[1:]]),
            file_name="ai-talks-chat.json",
            mime="application/json",
        )


def show_chat(ai_content: str, user_text: str) -> None:
    if ai_content not in st.session_state.generated:
        # store the ai content
        st.session_state.past.append(user_text)
        st.session_state.generated.append(ai_content)
    if st.session_state.generated:
        for i in range(len(st.session_state.generated)):
            message(st.session_state.past[i], is_user=True, key=str(i) + "_user", seed=st.session_state.seed)
            message("", key=str(i), seed=st.session_state.seed)
            st.markdown(st.session_state.generated[i])
            st.caption(f"""
                {st.session_state.locale.tokens_count}{st.session_state.total_tokens[i]} |
                {st.session_state.locale.message_cost}{st.session_state.costs[i]:.5f}$
            """, help=f"{st.session_state.locale.total_cost}{sum(st.session_state.costs):.5f}$")


def calc_cost(usage: dict) -> None:
    total_tokens = usage.get("total_tokens")
    prompt_tokens = usage.get("prompt_tokens")
    completion_tokens = usage.get("completion_tokens")
    st.session_state.total_tokens.append(total_tokens)
    # pricing logic: https://openai.com/pricing#language-models
    if st.session_state.model == "gpt-3.5-turbo":
        cost = total_tokens * 0.002 / 1000
    else:
        cost = (prompt_tokens * 0.03 + completion_tokens * 0.06) / 1000
    st.session_state.costs.append(cost)


def show_gpt_conversation() -> None:
    try:
        completion = create_gpt_completion(st.session_state.model, st.session_state.messages)
        ai_content = completion.get("choices")[0].get("message").get("content")
        calc_cost(completion.get("usage"))
        st.session_state.messages.append({"role": "assistant", "content": ai_content})
        if ai_content:
            show_chat(ai_content, st.session_state.user_text)
            st.divider()
            show_audio_player(ai_content)
    except InvalidRequestError as err:
        if err.code == "context_length_exceeded":
            st.session_state.messages.pop(1)
            if len(st.session_state.messages) == 1:
                st.session_state.user_text = ""
            show_conversation()
        else:
            st.error(err)
    except (OpenAIError, UnboundLocalError) as err:
        st.error(err)


def show_conversation() -> None:
    if st.session_state.messages:
        st.session_state.messages.append({"role": "user", "content": st.session_state.user_text})
    else:
        ai_role = f"{st.session_state.locale.ai_role_prefix} {st.session_state.role}. {st.session_state.locale.ai_role_postfix}"  # NOQA: E501
        st.session_state.messages = [
            {"role": "system", "content": ai_role},
            {"role": "user", "content": st.session_state.user_text},
        ]
    show_gpt_conversation()
