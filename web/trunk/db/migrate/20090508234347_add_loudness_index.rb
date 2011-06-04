class AddLoudnessIndex < ActiveRecord::Migration
  def self.up
    add_column :measures, :loudness_index, :integer
    add_index :measures, :loudness_index
  end

  def self.down
  end
end
