import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-root',
  imports: [RouterLink, RouterOutlet, MatToolbarModule, MatButtonModule],
  templateUrl: './app-shell.html',
  styleUrl: './app.scss'
})
export class App {}
