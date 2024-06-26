package tj.horner.villagergpt.conversation.pipeline.producers

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import org.bukkit.configuration.Configuration
import tj.horner.villagergpt.conversation.VillagerConversation
import tj.horner.villagergpt.conversation.pipeline.ConversationMessageProducer

class OpenAIMessageProducer(config: Configuration) : ConversationMessageProducer {
    private val openAIKey = config.getString("openai-key")!!
    private val openAIModel = config.getString("openai-model") ?: "gpt-3.5-turbo"
    private val openAIUrl = config.getString("openai-url") ?: "https://api.openai.com/v1/"
    private val openAIQueryParam = config.getString("openai-query-param") ?: ""
    private val openAIHeaders = config.getString("openai-headers") ?: ""
    private var openAI: OpenAI

    private val model = ModelId(openAIModel)

    init {
        var queryMap: Map<String, String> = emptyMap()
        var headerMap: Map<String, String> = emptyMap()

        if (openAIQueryParam.isNotEmpty()) {
            queryMap =
                    openAIQueryParam
                            .split(",")
                            .map {
                                val (key, value) = it.split("=")
                                key to value
                            }
                            .toMap()
        }

        if (openAIHeaders.isNotEmpty()) {
            headerMap =
                    openAIHeaders
                            .split(",")
                            .map {
                                val (key, value) = it.split("=")
                                key to value
                            }
                            .toMap()
        }

        var openAIHost = OpenAIHost(baseUrl = openAIUrl, queryParams = queryMap)

        openAI = OpenAI(OpenAIConfig(host = openAIHost, token = openAIKey, headers = headerMap))
    }

    @OptIn(BetaOpenAI::class)
    override suspend fun produceNextMessage(conversation: VillagerConversation): String {
        val request =
                ChatCompletionRequest(
                        model = model,
                        messages = conversation.messages,
                        temperature = 0.7,
                        user = conversation.player.uniqueId.toString()
                )

        val completion = openAI.chatCompletion(request)
        return completion.choices[0].message.content ?: ""
    }
}
