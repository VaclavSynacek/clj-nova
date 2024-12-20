# clj-nova

A minimal wrapper around the Amazon Nova LLMs called through Bedrock APIs, intended to be used in scripts (babashka or small clojure programs).

## Design decision:
* minimal
* good for scripting:
  * no support for streaming, which just complicates matters in scripts (as opposed to interactive tools where streaming is essential)
* same api as [clj-claude](https://github.com/VaclavSynacek/clj-claude) wrapper
  library (where possible)

## Feature status
- [ ] ~~streaming response~~
- [X] call through [AWS Bedrock API](https://aws.amazon.com/bedrock/)
- [ ] [Tool use](https://docs.aws.amazon.com/nova/latest/userguide/tool-use.html)
- [ ] [Prompt caching](https://docs.aws.amazon.com/bedrock/latest/userguide/prompt-caching.html)
- [ ] [Message batches](https://docs.aws.amazon.com/bedrock/latest/userguide/batch-inference.html)

## Usage
See `(comment` block at the end of [clj-nova.scripting](https://github.com/VaclavSynacek/clj-nova/blob/master/src/clj_nova/scripting.clj) namespace
