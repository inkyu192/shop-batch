package com.toy.shopbatch.job;

import com.toy.shopbatch.domain.Member;
import com.toy.shopbatch.domain.Member2;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MemberStatisticsJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job memberStatisticsJob(Step memberStep1, Step memberStep2) {
        return new JobBuilder("memberStatisticsJob", jobRepository)
                .start(memberStep1)
                .next(memberStep2)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step memberStep1() {
        return new StepBuilder("step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("==========================");
                    log.info("step1");
                    log.info("==========================");
                    return RepeatStatus.FINISHED;
                }, platformTransactionManager)
                .build();
    }

    @Bean
    public Step memberStep2() {
        int chunkSize = 2;

        return new StepBuilder("step1", jobRepository)
                .<Member, Member2>chunk(chunkSize, platformTransactionManager)
                .reader(new JpaPagingItemReaderBuilder<Member>()
                        .name("memberJpaPagingItemReader")
                        .entityManagerFactory(entityManagerFactory)
                        .pageSize(chunkSize)
                        .queryString("select m from Member m")
                        .build()
                )
                .processor(Member2::createMember2)
                .writer(new JpaItemWriterBuilder<Member2>()
                        .entityManagerFactory(entityManagerFactory)
                        .build())
                .build();
    }
}
