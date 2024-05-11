package com.example.simplemathquiz

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Random

class MainActivity : AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var gamelayout: LinearLayout
    private lateinit var dashbord: LinearLayout
    private lateinit var questionTextView: TextView
    private lateinit var option1Button: Button
    private lateinit var option2Button: Button
    private lateinit var option3Button: Button
    private lateinit var option4Button: Button
    private lateinit var timerTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var questionNumberTextView: Button
    private var currentQuestionNumber: Int = 1

    private val random = Random()
    private var score = 0
    private var currentQuestionIndex = 0
    private lateinit var currentQuestion: Question
    private var timer: CountDownTimer? = null

    private lateinit var scoreTextView: TextView
    private lateinit var title: TextView
    private val correctColor: Int by lazy { ContextCompat.getColor(this, R.color.correctColor) }
    private val incorrectColor: Int by lazy { ContextCompat.getColor(this, R.color.incorrectColor) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startButton = findViewById(R.id.start_button)
        gamelayout = findViewById(R.id.gamelayout)
        dashbord = findViewById(R.id.dashbord)
        progressBar = findViewById(R.id.progressBar)
        timerTextView = findViewById(R.id.timerTextView)
        scoreTextView = findViewById(R.id.scoreTextView)
        title = findViewById(R.id.title)
        questionNumberTextView = findViewById(R.id.questionNumberTextView)


        val viewScoreHistoryButton: Button = findViewById(R.id.viewScoreHistoryButton)
        viewScoreHistoryButton.setOnClickListener {
            showScoreHistory()
        }

        gamelayout.visibility = View.GONE

        startButton.setOnClickListener {

            val durationInMillis: Long = 10000
            val intervalInMillis: Long = 100

            object : CountDownTimer(durationInMillis, intervalInMillis) {

                override fun onTick(millisUntilFinished: Long) {
                    val progress = (millisUntilFinished * 100 / durationInMillis).toInt()
                    progressBar.progress = progress

                    val secondsRemaining = (millisUntilFinished / 1000).toInt()
                    timerTextView.text = secondsRemaining.toString()
                }

                override fun onFinish() {
                    timerTextView.text = "0"
                }
            }.start()
        }

        questionTextView = findViewById(R.id.questionTextView)
        option1Button = findViewById(R.id.option1Button)
        option2Button = findViewById(R.id.option2Button)
        option3Button = findViewById(R.id.option3Button)
        option4Button = findViewById(R.id.option4Button)
        timerTextView = findViewById(R.id.timerTextView)
        progressBar = findViewById(R.id.progressBar)

        startButton.setOnClickListener {
            startGame()
        }

        option1Button.setOnClickListener { onOptionSelected(it) }
        option2Button.setOnClickListener { onOptionSelected(it) }
        option3Button.setOnClickListener { onOptionSelected(it) }
        option4Button.setOnClickListener { onOptionSelected(it) }
    }

    private fun startGame() {
        option1Button.setBackgroundResource(R.drawable.rounded_button_background)
        option2Button.setBackgroundResource(R.drawable.rounded_button_background)
        option3Button.setBackgroundResource(R.drawable.rounded_button_background)
        option4Button.setBackgroundResource(R.drawable.rounded_button_background)

        dashbord.visibility = View.GONE
        gamelayout.visibility = View.VISIBLE
        score = 0
        currentQuestionIndex = 0
        showNextQuestion()
        score = 0
        updateScoreDisplay()
    }

    private fun showScoreHistory() {
        val sharedPreferences = getSharedPreferences("Scores", Context.MODE_PRIVATE)
        val score = sharedPreferences.getInt("score", 0)
        // Display score history, for example, in a dialog
        AlertDialog.Builder(this)
            .setTitle("Score History")
            .setMessage("Your previous score: $score")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showNextQuestion() {
        currentQuestion = generateRandomQuestion()
        updateQuestionUI()
        startTimer()
        questionNumberTextView.text = "    Question $currentQuestionNumber/50    "
        currentQuestionNumber++
    }

    private fun updateQuestionUI() {
        questionTextView.text = currentQuestion.question
        val options = currentQuestion.options
        option1Button.text = options[0]
        option2Button.text = options[1]
        option3Button.text = options[2]
        option4Button.text = options[3]
    }

    private fun startTimer() {
        timer?.cancel()
        var timeLeft = 10000L
        progressBar.progress = 100
        val duration = 10000L // Total duration of the timer in milliseconds
        val interval = 50L // Update interval in milliseconds

        timer = object : CountDownTimer(duration, interval) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = ((duration - millisUntilFinished) * 100 / duration).toInt()
                progressBar.progress = progress
                timeLeft = millisUntilFinished
                timerTextView.text = "${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                progressBar.progress = 100
                timerTextView.text = "Time's up!"
                endGame()
            }
        }.start()
    }

    //generate random question
    private fun generateRandomQuestion(): Question {
        val num1 = random.nextInt(100)
        val num2 = random.nextInt(100)
        val operation = random.nextInt(4) // 0: addition, 1: subtraction, 2: multiplication, 3: division

        val correctAnswer: Int
        val questionText: String
        when (operation) {
            0 -> {
                questionText = "$num1 + $num2?"
                correctAnswer = num1 + num2
            }
            1 -> {
                questionText = "$num1 - $num2?"
                correctAnswer = num1 - num2
            }
            2 -> {
                questionText = "$num1 * $num2?"
                correctAnswer = num1 * num2
            }
            else -> {
                // For division, make sure num2 is not 0
                val divisor = if (num2 != 0) num2 else 1
                questionText = "$num1 / $divisor?"
                correctAnswer = num1 / divisor
            }
        }

        val options = generateOptions(correctAnswer)

        return Question(questionText, options, options.indexOf(correctAnswer.toString()))
    }

    // generate the option value
    private fun generateOptions(correctAnswer: Int): List<String> {
        val options = mutableListOf<String>()
        options.add(correctAnswer.toString())

        while (options.size < 4) {
            val option: String = when (random.nextInt(4)) {
                0 -> (correctAnswer + random.nextInt(20) - 10).toString()
                1 -> (correctAnswer + random.nextInt(20) - 10).toString()
                2 -> (correctAnswer * (random.nextInt(3) + 1)).toString()
                else -> ((correctAnswer + random.nextInt(6) - 2) / (random.nextInt(3) + 1)).toString()
            }
            if (!options.contains(option)) {
                options.add(option)
            }
        }

        return options.shuffled()
    }

    private fun endGame() {
        Toast.makeText(this, "Game Over! Your score: $score", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            dashbord.visibility = View.VISIBLE
            gamelayout.visibility = View.GONE
        }, 1000)

        timer?.cancel()
        currentQuestionNumber = 1
        title.text = "  Game Over!  \n\n Score: $score "
        startButton.text = "    Play Again     "

        // Save score to SharedPreferences
        val sharedPreferences = getSharedPreferences("Scores", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("score", score)
        editor.apply()

        // Hide the history button
        val viewScoreHistoryButton: Button = findViewById(R.id.viewScoreHistoryButton)
        viewScoreHistoryButton.visibility = View.GONE
    }

    fun onOptionSelected(view: View) {
        val selectedOption = when (view.id) {
            R.id.option1Button -> 0
            R.id.option2Button -> 1
            R.id.option3Button -> 2
            R.id.option4Button -> 3
            else -> -1
        }

        if (selectedOption == currentQuestion.correctOption) {
            score += 10  // Increase the score by 10
            updateScoreDisplay()
            view.setBackgroundColor(correctColor)
            Toast.makeText(this, "Correct! Score +10", Toast.LENGTH_SHORT).show()
        } else {
            view.setBackgroundColor(incorrectColor)
            Toast.makeText(this, "Wrong! Game Over.", Toast.LENGTH_SHORT).show()
            endGame()
            return
        }

        Handler(Looper.getMainLooper()).postDelayed({
            view.setBackgroundResource(R.drawable.rounded_button_background)
            currentQuestionIndex++
            showNextQuestion()
        }, 1000)
    }

    private fun updateScoreDisplay() {
        scoreTextView.text = "Score: $score"
    }
}
