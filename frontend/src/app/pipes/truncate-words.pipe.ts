import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
  name: "truncateWords",
  standalone: true, // Marked as standalone
})
export class TruncateWordsPipe implements PipeTransform {
  transform(value: string, limit = 100): string {
    if (!value || value.length <= limit) return value;
    const sliced = value.slice(0, limit);
    return sliced.substring(0, sliced.lastIndexOf(" "));
  }
}
