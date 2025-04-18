# Mathify

Mathify is your math tutor which helps you solve complex math’s problem.

**Stuck on a math problem?** Just Take a picture, type, or speak out loud your question, and get a clear and definitive answers fast and efficiently.

Mathify is built using [Groq](https://groq.com/) API

## Technologies used

- [android studio](https://developer.android.com/studio)
- [java](https://www.java.com/en/), [xml](https://www.w3schools.com/xml/xml_whatis.asp)
- ImgDB
- [ML kit](https://developers.google.com/ml-kit)
- Model: **meta-llama/llama-4-scout-17b-16e-instruct**

## API

Put your own GROQ_API_KEY and run it 

```java
curl https://api.groq.com/openai/v1/chat/completions -s \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $GROQ_API_KEY" \
-d '{
"model": "llama-3.3-70b-versatile",
"messages": [{
    "role": "user",
    "content": "Explain the importance of fast language models"
}]
}'
```

## Features

- Instant replies
- One stop to all your math’s doubts
- Speech to text support
- Image prompt support
- Modern and sleek UI
- Prompt history

<div align="center">
  <img src="./image1" alt="Image 1" width="300" style="margin-right: 200px;"/>
  <img src="./image2" alt="Image 2" width="300"/>
</div>


## Credits

This is project is built for participation in [HACKHAZARDS '25](https://hackhazards25.devfolio.co/)
