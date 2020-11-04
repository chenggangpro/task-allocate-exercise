package pro.chenggang.exercise.task_allocate_exercise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration;

@SpringBootApplication(exclude = {IntegrationAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class})
public class TaskAllocateExerciseApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskAllocateExerciseApplication.class, args);
	}

}
