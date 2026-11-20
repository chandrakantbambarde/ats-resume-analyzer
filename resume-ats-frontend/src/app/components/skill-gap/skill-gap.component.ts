import { Component, ChangeDetectorRef, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ResumeService } from '../../services/auth/service/resume.service';
import { SkillGapResponse, MatchedSkill, MissingSkill, SkillLearningPath } from '../../models/user/model/analysis.model';

@Component({
  selector: 'app-skill-gap',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './skill-gap.component.html',
  styleUrls: ['./skill-gap.component.css']
})
export class SkillGapComponent implements OnInit, OnDestroy {
  selectedFile: File | null = null;
  jobDescription: string = '';
  result: SkillGapResponse | null = null;
  loading: boolean = false;

  loadStep: number = 0;
  loadProgress: number = 0;
  animatedScore: number = 0;
  private scoreInterval: any;

  // Active Tab: 'overview' | 'priority' | 'recommendations' | 'learning' | 'resumeSkills'
  activeTab: string = 'overview';

  // Filters for priority gap tab
  priorityFilter: 'ALL' | 'HIGH' | 'MEDIUM' | 'LOW' = 'ALL';
  searchTerm: string = '';

  constructor(
    private resumeService: ResumeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {}

  ngOnDestroy(): void {
    if (this.scoreInterval) {
      clearInterval(this.scoreInterval);
    }
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file && file.type === 'application/pdf') {
      this.selectedFile = file;
      this.cdr.detectChanges();
    } else if (file) {
      alert('Please upload a valid PDF resume.');
    }
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    const file = event.dataTransfer?.files[0];
    if (file && file.type === 'application/pdf') {
      this.selectedFile = file;
      this.cdr.detectChanges();
    } else if (file) {
      alert('Please drop a valid PDF resume.');
    }
  }

  removeFile(event: Event): void {
    event.stopPropagation();
    this.selectedFile = null;
    this.cdr.detectChanges();
  }

  fillSampleJd(): void {
    this.jobDescription = `Senior Full Stack Java & Cloud Engineer
Responsibilities:
- Build robust microservices using Java 17+, Spring Boot, Hibernate, and REST APIs.
- Design responsive front-end interfaces using Angular, TypeScript, and modern CSS.
- Deploy containerized services with Docker, Kubernetes, and Helm on AWS cloud infrastructure.
- Implement streaming & event-driven architecture using Apache Kafka and Redis caching.
- Manage relational database schemas with MySQL and PostgreSQL.
- Maintain automated CI/CD pipelines with GitHub Actions and Docker.
- Write thorough unit and integration tests with JUnit 5 and Mockito.

Required Skills:
- Java, Spring Boot, Microservices, REST API, Angular, TypeScript, MySQL, Docker, Kubernetes, CI/CD, JUnit

Preferred Qualifications:
- AWS, Kafka, Redis, PostgreSQL, GraphQL, Agile/Scrum`;
    this.cdr.detectChanges();
  }

  analyzeSkillGap(): void {
    if (!this.selectedFile || !this.jobDescription.trim()) return;

    this.loading = true;
    this.result = null;
    this.loadStep = 0;
    this.loadProgress = 0;
    this.activeTab = 'overview';
    this.priorityFilter = 'ALL';
    this.searchTerm = '';
    this.cdr.detectChanges();

    this.startLoadingSequence();

    this.resumeService.analyzeSkillGap(this.selectedFile, this.jobDescription).subscribe({
      next: (res: SkillGapResponse) => {
        this.loadProgress = 100;
        this.loadStep = 4;
        this.cdr.detectChanges();

        setTimeout(() => {
          this.loading = false;
          this.result = res;
          this.animatedScore = 0;
          this.cdr.detectChanges();
          setTimeout(() => this.animateScore(res.skillMatchPercentage || 0), 200);
        }, 500);
      },
      error: (err) => {
        console.error('Skill Gap Analysis error:', err);
        this.loading = false;
        this.cdr.detectChanges();
        alert('Failed to analyze skill gap. Please ensure the backend server is running on http://localhost:8080.');
      }
    });
  }

  private startLoadingSequence(): void {
    const steps = [
      { step: 1, progress: 25, delay: 400 },
      { step: 2, progress: 50, delay: 1200 },
      { step: 3, progress: 75, delay: 2400 },
      { step: 4, progress: 92, delay: 3600 }
    ];
    steps.forEach(({ step, progress, delay }) => {
      setTimeout(() => {
        if (this.loading) {
          this.loadStep = step;
          this.loadProgress = progress;
          this.cdr.detectChanges();
        }
      }, delay);
    });
  }

  private animateScore(target: number): void {
    if (this.scoreInterval) clearInterval(this.scoreInterval);
    const steps = 45;
    const increment = target / steps;
    let current = 0;
    this.scoreInterval = setInterval(() => {
      current += increment;
      if (current >= target) {
        this.animatedScore = target;
        clearInterval(this.scoreInterval);
      } else {
        this.animatedScore = Math.floor(current);
      }
      this.cdr.detectChanges();
    }, 1200 / steps);
  }

  get filteredMissingSkills(): MissingSkill[] {
    if (!this.result || !this.result.missingSkills) return [];
    return this.result.missingSkills.filter(item => {
      const matchesFilter = this.priorityFilter === 'ALL' || item.priority === this.priorityFilter;
      const matchesSearch = !this.searchTerm ||
        item.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        item.category.toLowerCase().includes(this.searchTerm.toLowerCase());
      return matchesFilter && matchesSearch;
    });
  }

  get filteredMatchedSkills(): MatchedSkill[] {
    if (!this.result || !this.result.matchedSkills) return [];
    return this.result.matchedSkills.filter(item => {
      return !this.searchTerm ||
        item.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        item.category.toLowerCase().includes(this.searchTerm.toLowerCase());
    });
  }

  getScoreColor(score: number): string {
    if (score >= 80) return '#22c55e';
    if (score >= 60) return '#f59e0b';
    return '#ef4444';
  }

  getStrokeDashOffset(score: number): number {
    return 314 - (score / 100) * 314;
  }

  getMatchGrade(score: number): string {
    if (score >= 85) return 'Strong Alignment';
    if (score >= 70) return 'Solid Match';
    if (score >= 50) return 'Moderate Gaps';
    return 'Significant Gaps';
  }

  getPriorityBadgeClass(priority: string): string {
    switch (priority?.toUpperCase()) {
      case 'HIGH': return 'badge-priority-high';
      case 'MEDIUM': return 'badge-priority-medium';
      case 'LOW': return 'badge-priority-low';
      default: return 'badge-priority-medium';
    }
  }

  getTypeBadgeClass(type: string): string {
    return type?.toUpperCase() === 'REQUIRED' ? 'badge-type-required' : 'badge-type-preferred';
  }

  resetForm(): void {
    if (this.scoreInterval) clearInterval(this.scoreInterval);
    this.selectedFile = null;
    this.jobDescription = '';
    this.result = null;
    this.loading = false;
    this.loadStep = 0;
    this.loadProgress = 0;
    this.animatedScore = 0;
    this.cdr.detectChanges();
  }

  exportReport(): void {
    if (!this.result) return;

    let text = `=====================================================
AI SKILL GAP ANALYSIS REPORT
=====================================================
Resume: ${this.result.fileName || 'Uploaded Resume'}
Generated on: ${new Date().toLocaleString()}

MATCH METRICS:
- Overall Skill Match:    ${this.result.skillMatchPercentage}% (${this.getMatchGrade(this.result.skillMatchPercentage)})
- Required Skills Match:  ${this.result.requiredMatchPercentage}% (${this.result.matchedSkills.filter(s => s.type === 'REQUIRED').length} / ${this.result.requiredSkills?.length || 0})
- Preferred Skills Match: ${this.result.preferredMatchPercentage}% (${this.result.matchedSkills.filter(s => s.type === 'PREFERRED').length} / ${this.result.preferredSkills?.length || 0})
- High Priority Gaps:     ${this.result.highPriorityCount}
- Medium Priority Gaps:   ${this.result.mediumPriorityCount}
- Low Priority Gaps:      ${this.result.lowPriorityCount}

=====================================================
1. MATCHED SKILLS (${this.result.matchedSkills?.length || 0})
=====================================================
`;
    this.result.matchedSkills?.forEach((s, idx) => {
      text += `${idx + 1}. [${s.type}] ${s.name} (${s.category}) - ${s.context || 'Verified in resume'}\n`;
    });

    text += `\n=====================================================
2. MISSING SKILLS & GAP PRIORITIES (${this.result.missingSkills?.length || 0})
=====================================================\n`;
    this.result.missingSkills?.forEach((s, idx) => {
      text += `${idx + 1}. [${s.priority} PRIORITY | ${s.type}] ${s.name} (${s.category})\n   Reason: ${s.reason}\n`;
    });

    text += `\n=====================================================
3. AI-BASED PERSONALIZED RECOMMENDATIONS
=====================================================\n`;
    this.result.personalizedRecommendations?.forEach((rec, idx) => {
      text += `${idx + 1}. ${rec}\n`;
    });

    text += `\n=====================================================
4. LEARNING RESOURCES & PRACTICE PROJECTS
=====================================================\n`;
    this.result.learningPaths?.forEach((lp, idx) => {
      text += `\n-----------------------------------------------------
SKILL: ${lp.skill} (Priority: ${lp.priority} | Est. Time: ${lp.estimatedTime})
-----------------------------------------------------\n`;
      text += `Learning Resources:\n`;
      lp.resources?.forEach(r => {
        text += `  * [${r.type}] ${r.title} (${r.platform})\n    URL: ${r.url}\n    Note: ${r.description}\n`;
      });
      text += `Practice Projects:\n`;
      lp.practiceProjects?.forEach(p => {
        text += `  * Project: ${p.title} [Difficulty: ${p.difficulty}]\n    Description: ${p.description}\n    Deliverables: ${p.keyDeliverables?.join(', ')}\n`;
      });
    });

    text += `\n=====================================================
Report generated by ATSScan Skill Gap Intelligence
=====================================================`;

    const blob = new Blob([text], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `Skill_Gap_Analysis_${new Date().toISOString().slice(0, 10)}.txt`;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
